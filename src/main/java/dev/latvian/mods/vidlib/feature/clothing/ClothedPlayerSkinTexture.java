package dev.latvian.mods.vidlib.feature.clothing;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.PersistentPixelTexture;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ClothedPlayerSkinTexture extends PersistentPixelTexture {
	public record ClothedPlayerSkinCache(NativeImage setImage, Map<ResourceLocation, ClothedPlayerSkinTexture> cache) {
	}

	public static final Lazy<NativeImage> MISSING = Lazy.of(() -> {
		var img = new NativeImage(64, 64, true);

		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 64; y++) {
				if (x < 8 && y < 8) {
					continue;
				}

				img.setPixel(x, y, ((x % 2) == (y % 2)) ? 0xFF000000 : 0xFFFF00FF);
			}
		}

		return img;
	});

	public static final EnumMap<PlayerSkin.Model, Map<ResourceLocation, Optional<ClothingPartImage>>> PART_CACHE = new EnumMap<>(PlayerSkin.Model.class);
	public static final EnumMap<PlayerSkin.Model, Map<ResourceLocation, NativeImage>> SET_CACHE = new EnumMap<>(PlayerSkin.Model.class);
	public static final EnumMap<PlayerSkin.Model, Map<ResourceLocation, ClothedPlayerSkinCache>> CLOTHED_PLAYER_SKIN_CACHE = new EnumMap<>(PlayerSkin.Model.class);

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload() {
		MISSING.forget(NativeImage::close);

		for (var map : PART_CACHE.values()) {
			for (var optional : map.values()) {
				optional.ifPresent(ClothingPartImage::close);
			}
		}

		for (var cache : SET_CACHE.values()) {
			cache.values().forEach(NativeImage::close);
		}

		MiscClientUtils.BUILTIN_SKIN_IMAGE_MAP.values().forEach(NativeImage::close);
		MiscClientUtils.BUILTIN_SKIN_IMAGE_MAP.clear();

		Stream.concat(Stream.of(SkinTexture.DEFAULT_WIDE), Stream.of(SkinTexture.DEFAULT_SLIM)).forEach(skin -> {
			try (var in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(skin.asset().texturePath()).open()) {
				var img = NativeImage.read(in);

				if (img.getWidth() == img.getHeight() && img.getWidth() >= 4) {
					MiscClientUtils.BUILTIN_SKIN_IMAGE_MAP.put(skin.asset().texturePath(), img);
				}
			} catch (Exception ignore) {
			}
		});

		PART_CACHE.clear();
		SET_CACHE.clear();
		CLOTHED_PLAYER_SKIN_CACHE.clear();
		ClothingPresets.ready = false;
	}

	@Nullable
	public static NativeImage computePart(Minecraft mc, PlayerSkin.Model model, ResourceLocation asset, Gradient gradient) {
		var map = PART_CACHE.computeIfAbsent(model, m -> new Object2ObjectOpenHashMap<>());
		var tex = map.get(asset);

		if (tex == null) {
			try {
				tex = Optional.of(new ClothingPartImage(mc.getResourceManager(), model, asset));
			} catch (Exception ex) {
				VidLib.LOGGER.warn("Failed to create a clothing part image " + asset, ex);
				tex = Optional.empty();
			}

			map.put(asset, tex);
		}

		if (tex.isPresent()) {
			return tex.get().withGradient(gradient);
		}

		return null;
	}

	public static NativeImage computeSet(Minecraft mc, PlayerSkin.Model model, ClothingSet set) {
		var map = SET_CACHE.computeIfAbsent(model, m -> new Object2ObjectOpenHashMap<>());
		var uniqueId = set.getUniqueId();
		var img = map.get(uniqueId);

		if (img == null) {
			var layers = new ArrayList<NativeImage>(set.parts.size());

			for (var part : set.parts) {
				var partImg = computePart(mc, model, part.texture(), part.colors());

				if (partImg != null) {
					layers.add(partImg);
				} else {
					layers.add(MISSING.get());
				}
			}

			img = MiscClientUtils.layeredImage(layers, true);
			map.put(uniqueId, img);
		}

		return img;
	}

	public static ResourceLocation computeClothedPlayerSkin(Minecraft mc, ResourceLocation skinId, NativeImage skinImage, PlayerSkin.Model model, ClothingSet clothing) {
		var map = CLOTHED_PLAYER_SKIN_CACHE.computeIfAbsent(model, m -> new Object2ObjectOpenHashMap<>());
		var uniqueId = clothing.getUniqueId();
		var cache = map.get(uniqueId);

		if (cache == null) {
			cache = new ClothedPlayerSkinCache(computeSet(mc, model, clothing), new Object2ObjectOpenHashMap<>());
			map.put(uniqueId, cache);
		}

		var tex = cache.cache.get(skinId);

		if (tex == null) {
			var pixels = MiscClientUtils.layeredImage(List.of(skinImage, cache.setImage), false);
			tex = new ClothedPlayerSkinTexture(uniqueId.withPath("textures/vidlib/generated/clothed_player/" + skinId.getPath() + "/" + uniqueId.getPath() + "/" + model.id() + ".png"), pixels);
			mc.getTextureManager().registerAndLoad(tex.resourceId(), tex);
			cache.cache.put(skinId, tex);
		}

		return tex.resourceId();
	}

	@Nullable
	public static ResourceLocation replace(Minecraft mc, ResourceLocation skinTexture, PlayerSkin.Model model, PlayerClothing playerClothing) {
		if (ClothingPresets.ready && playerClothing != PlayerClothing.NONE) {
			var skinImage = MiscClientUtils.SKIN_IMAGE_MAP.get(skinTexture);

			if (skinImage == null) {
				skinImage = MiscClientUtils.BUILTIN_SKIN_IMAGE_MAP.get(skinTexture);
			}

			if (skinImage != null) {
				var clothing = playerClothing.resolve();

				if (!clothing.parts.isEmpty()) {
					return computeClothedPlayerSkin(mc, skinTexture, skinImage, model, clothing);
				}
			}
		}

		return null;
	}

	@Nullable
	public static ResourceLocation replace(Minecraft mc, PlayerSkin playerSkin, PlayerClothing playerClothing) {
		return replace(mc, playerSkin.texture(), playerSkin.model(), playerClothing);
	}

	public ClothedPlayerSkinTexture(ResourceLocation location, NativeImage pixels) {
		super(location);
		this.pixels = pixels;
	}
}
