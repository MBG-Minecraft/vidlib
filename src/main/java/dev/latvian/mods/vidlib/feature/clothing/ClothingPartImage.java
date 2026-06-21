package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.color.GradientReference;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import it.unimi.dsi.fastutil.ints.Int2FloatArrayMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.PlayerModelType;

import java.util.Map;

public class ClothingPartImage {
	public static NativeImage toModel(NativeImage src, PlayerModelType model) {
		if (model == PlayerModelType.SLIM) {
			var dst = new NativeImage(64, 64, true);
			dst.copyFrom(src);
			return dst;
		}

		return src;
	}

	public final NativeImage pixels;
	public final Int2FloatMap gradientValues;
	public final Map<Identifier, NativeImage> gradientRefCache;
	public final Int2ObjectMap<NativeImage> colorCache;

	public ClothingPartImage(ResourceManager resourceManager, PlayerModelType model, Identifier asset) throws Exception {
		this.gradientRefCache = new Object2ObjectOpenHashMap<>(4);
		this.colorCache = new Int2ObjectOpenHashMap<>(0);

		var modelResource = resourceManager.getResource(asset.withPath("textures/vidlib/clothing/" + asset.getPath() + "_" + model.getSerializedName() + ".png")).orElse(null);
		var resource = modelResource != null ? modelResource : resourceManager.getResourceOrThrow(asset.withPath("textures/vidlib/clothing/" + asset.getPath() + ".png"));

		try (var in = resource.open()) {
			var gradientValues = new Int2FloatOpenHashMap();
			var clearSet = new IntOpenHashSet(1);
			var src = NativeImage.read(in);

			for (int row = 0; row < 8; ++row) {
				for (int col = 0; col < 4; ++col) {
					int msrc = src.getPixel(col * 2, row);
					int mdst = src.getPixel(col * 2 + 1, row);

					if (ARGB.alpha(msrc) > 0) {
						src.setPixel(col * 2, row, 0);
						src.setPixel(col * 2 + 1, row, 0);

						if (ARGB.alpha(mdst) > 0) {
							gradientValues.put(msrc & 0xFFFFFF, ARGB.red(mdst) / 255F);
						} else {
							clearSet.add(msrc & 0xFFFFFF);
						}
					}
				}
			}

			pixels = modelResource != null ? src : toModel(src, model);

			if (!clearSet.isEmpty()) {
				for (int x = 0; x < pixels.getWidth(); x++) {
					for (int y = 0; y < pixels.getHeight(); y++) {
						if (clearSet.contains(pixels.getPixel(x, y) & 0xFFFFFF)) {
							pixels.setPixel(x, y, 0x01010101);
						}
					}
				}
			}

			this.gradientValues = gradientValues.isEmpty() ? Int2FloatMaps.EMPTY_MAP : new Int2FloatArrayMap(gradientValues);

			if (src != pixels) {
				src.close();
			}
		}
	}

	private NativeImage remap(Gradient gradient) {
		var g = gradient.optimize();
		var remap = new Int2IntOpenHashMap();

		for (var entry : gradientValues.int2FloatEntrySet()) {
			if (entry.getFloatValue() < 0F) {
				remap.put(entry.getIntKey(), 0x01010101);
			} else {
				remap.put(entry.getIntKey(), 0xFF000000 | g.get(entry.getFloatValue()).argb());
			}
		}

		return MiscClientUtils.remapImage(pixels, remap);
	}

	public NativeImage withGradient(Gradient gradient) {
		if (gradient == Color.TRANSPARENT && gradientValues.isEmpty()) {
			return pixels;
		} else if (gradient instanceof GradientReference ref) {
			return gradientRefCache.computeIfAbsent(ref.id(), id -> remap(ref));
		} else if (gradient instanceof Color color) {
			return colorCache.computeIfAbsent(color.argb(), id -> remap(color));
		} else {
			return remap(gradient);
		}
	}

	public void close() {
		pixels.close();

		for (var img : gradientRefCache.values()) {
			img.close();
		}

		for (var img : colorCache.values()) {
			img.close();
		}
	}
}
