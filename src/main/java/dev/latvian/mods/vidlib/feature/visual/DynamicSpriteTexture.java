package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLSpriteContents;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class DynamicSpriteTexture extends AbstractTexture implements Dumpable, EphemeralTexture {
	public static final Lazy<Map<FluidType, SpriteKey>> STILL_FLUIDS = Lazy.identityMap(map -> {
		for (var entry : NeoForgeRegistries.FLUID_TYPES.entrySet()) {
			var info = IClientFluidTypeExtensions.of(entry.getValue());

			if (info.getStillTexture() != null) {
				map.put(entry.getValue(), SpriteKey.block(info.getStillTexture()));
			}
		}
	});

	public static final Lazy<Map<FluidType, SpriteKey>> FLOWING_FLUIDS = Lazy.identityMap(map -> {
		for (var entry : NeoForgeRegistries.FLUID_TYPES.entrySet()) {
			var info = IClientFluidTypeExtensions.of(entry.getValue());

			if (info.getFlowingTexture() != null) {
				map.put(entry.getValue(), SpriteKey.block(info.getFlowingTexture()));
			}
		}
	});

	private static final Map<ResourceLocation, Map<ResourceLocation, DynamicSpriteTexture>> MAP = new Object2ObjectOpenHashMap<>();

	public static void clearAtlas(ResourceLocation id) {
		VidLib.LOGGER.debug("Clearing DynamicSpriteTexture atlas " + id);
		MAP.remove(id);
	}

	public static void createTexture(TextureAtlasSprite sprite) {
		VidLib.LOGGER.debug("Created DynamicSpriteTexture " + sprite.atlasLocation() + "/" + sprite.contents().name());
		var map = MAP.computeIfAbsent(sprite.atlasLocation(), k -> new Object2ObjectOpenHashMap<>());
		var texture = map.computeIfAbsent(sprite.contents().name(), id -> new DynamicSpriteTexture(sprite));
		((VLSpriteContents) sprite.contents()).vl$setDynamicSpriteTexture(texture);
	}

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload() {
	}

	public static ResourceLocation get(SpriteKey key) {
		if (key.atlas() == SpriteKey.SPECIAL) {
			return key.sprite();
		}

		var map = MAP.get(key.atlas());

		if (map != null) {
			var tex = map.get(key.sprite());

			if (tex != null) {
				if (!tex.initialized) {
					var mc = Minecraft.getInstance();
					tex.initialize(mc);
					mc.getTextureManager().register(tex.path, tex);
					tex.initialized = true;
				}

				return tex.path;
			}
		}

		return key.dynamic();
	}

	@Nullable
	public static ResourceLocation getStillFluid(FluidType fluidType) {
		var key = STILL_FLUIDS.get().get(fluidType);
		return key == null ? null : get(key);
	}

	@Nullable
	public static ResourceLocation getFlowingFluid(FluidType fluidType) {
		var key = FLOWING_FLUIDS.get().get(fluidType);
		return key == null ? null : get(key);
	}

	public final TextureAtlasSprite sprite;
	public final SpriteKey key;
	public final ResourceLocation path;
	public boolean initialized;

	public DynamicSpriteTexture(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		this.key = SpriteKey.of(sprite.atlasLocation(), sprite.contents().name());
		this.path = key.dynamic();
	}

	private void initialize(Minecraft mc) {
		var contents = sprite.contents();
		int mipLevel = mc.options.mipmapLevels().get();

		if (mipLevel > 0) {
			contents.increaseMipLevel(mipLevel);
		}

		int w = contents.width();
		int h = contents.height();
		int m = contents.byMipLevel.length;

		var device = RenderSystem.getDevice();
		texture = device.createTexture(path::toString, TextureFormat.RGBA8, w, h, m);
		setFilter(false, true);
		setClamp(false);
		contents.uploadFirstFrame(0, 0, texture);
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) {
		if (texture != null) {
			TextureUtil.writeAsPNG(path, id.toDebugFileName(), texture, 0, IntUnaryOperator.identity());
		}
	}

	@Override
	public void close() {
		super.close();
		((VLSpriteContents) sprite.contents()).vl$setDynamicSpriteTexture(null);
		initialized = false;
	}
}
