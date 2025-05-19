package dev.latvian.mods.vidlib.feature.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.Lazy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class DynamicSpriteTexture extends AbstractTexture implements Dumpable, Tickable, EphemeralTexture {
	private static final SpriteResourceLoader SPRITE_LOADER = SpriteResourceLoader.create(List.of(AnimationMetadataSection.TYPE));

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

	private static final Map<SpriteKey, DynamicSpriteTexture> ALL = new HashMap<>();

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload() {
		ALL.clear();
	}

	public static ResourceLocation get(SpriteKey key) {
		if (key.atlas() == SpriteKey.SPECIAL) {
			return key.sprite();
		}

		var tex = ALL.get(key);

		if (tex == null) {
			tex = new DynamicSpriteTexture(key);
			var mc = Minecraft.getInstance();
			mc.getTextureManager().register(tex.path, tex);
			ALL.put(key, tex);

			try {
				tex.load(mc);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to load DynamicSpriteTexture " + key, ex);
			}
		}

		return tex.path;
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

	public final SpriteKey key;
	public final ResourceLocation path;
	public SpriteContents contents;
	public SpriteTicker ticker;

	public DynamicSpriteTexture(SpriteKey key) {
		this.key = key;
		this.path = key.dynamic();
	}

	private void load(Minecraft mc) throws Exception {
		contents = SPRITE_LOADER.loadSprite(key.sprite(), mc.getResourceManager().getResourceOrThrow(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(key.sprite())));

		if (contents == null) {
			VidLib.LOGGER.error("Failed to load DynamicSpriteTexture " + key);
			return;
		}

		int mipLevel = mc.options.mipmapLevels().get();

		if (mipLevel > 0) {
			contents.increaseMipLevel(mipLevel);
		}

		ticker = contents.createTicker();

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
	public void close() {
		super.close();

		if (contents != null) {
			contents.close();
			contents = null;
		}

		ticker = null;
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) {
		if (texture != null) {
			TextureUtil.writeAsPNG(path, id.toDebugFileName(), texture, 0, IntUnaryOperator.identity());
		}
	}

	@Override
	public void tick() {
		if (ticker != null && texture != null) {
			ticker.tickAndUpload(0, 0, texture);
		}
	}
}
