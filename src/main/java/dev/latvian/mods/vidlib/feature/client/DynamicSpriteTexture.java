package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.latvian.mods.vidlib.util.Lazy;
import dev.latvian.mods.vidlib.util.SpriteKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class DynamicSpriteTexture extends ReloadableTexture implements Dumpable, Tickable {
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

	public static DynamicSpriteTexture get(Minecraft mc, SpriteKey key) {
		var def = ALL.computeIfAbsent(key, DynamicSpriteTexture::new);

		if (def.contents == null) {
			def.contents = mc.getTextureAtlas(def.key.atlas()).apply(def.key.sprite()).contents();

			/*
			def.contents = new SpriteContents(
				def.contents.name(),
				new FrameSize(def.contents.width(), def.contents.height()),
				def.contents.getOriginalImage(),
				def.contents.metadata()
			);
			 */

			def.ticker = def.contents.createTicker();

			var atex = mc.getTextureManager().byPath.get(def.resourceId());

			if (!(atex instanceof DynamicSpriteTexture)) {
				mc.getTextureManager().registerAndLoad(def.resourceId(), def);
			}
		}

		return def;
	}

	@Nullable
	public static DynamicSpriteTexture getStillFluid(Minecraft mc, FluidType fluidType) {
		var key = STILL_FLUIDS.get().get(fluidType);
		return key == null ? null : get(mc, key);
	}

	@Nullable
	public static DynamicSpriteTexture getFlowingFluid(Minecraft mc, FluidType fluidType) {
		var key = FLOWING_FLUIDS.get().get(fluidType);
		return key == null ? null : get(mc, key);
	}

	public final SpriteKey key;
	public SpriteContents contents;
	public SpriteTicker ticker;

	public DynamicSpriteTexture(SpriteKey key) {
		super(key.dynamic());
		this.key = key;
	}

	@Override
	public TextureContents loadContents(ResourceManager manager) {
		return new TextureContents(new NativeImage(1, 1, false), new TextureMetadataSection(false, false));
	}

	@Override
	public void close() {
		super.close();
		contents = null;
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

	@Override
	public void doLoad(NativeImage image, boolean blur, boolean clamp) {
		update();
	}

	public void update() {
		if (contents != null) {
			int w = contents.width();
			int h = contents.height();
			int m = contents.byMipLevel.length;

			var device = RenderSystem.getDevice();
			texture = device.createTexture(resourceId()::toString, TextureFormat.RGBA8, w, h, m);
			setFilter(false, true);
			setClamp(false);
			contents.uploadFirstFrame(0, 0, texture);
			// device.createCommandEncoder().writeToTexture(texture, );
		}
	}
}
