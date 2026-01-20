package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.FramebufferUtils;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface LowQualityPlayerBodies {
	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("low_quality_player_bodies", () -> VidLibPaths.USER.get().resolve("low-quality-player-bodies"), TriState.TRUE);

	Lazy<RenderTarget> RENDER_TARGET = Lazy.of(() -> new TextureTarget("LowQualityPlayerBodiesCanvas", 64, 64, true));

	TexturedRenderType RENDER_TYPE = TexturedRenderType.internal(
		"low_quality_player_body",
		1536,
		true,
		true,
		RenderPipelines.ENTITY_CUTOUT_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.setOutputState(new RenderStateShard.OutputStateShard("low_quality_player_body", RENDER_TARGET))
			.createCompositeState(false)
	);

	static GalleryImage<UUID> get(Minecraft mc, UUID uuid) {
		return GALLERY.getRender(mc, uuid, PlayerProfiles::getName, LowQualityPlayerBodies::render, ImagePreProcessor.NONE);
	}

	private static NativeImage render(Minecraft mc, UUID uuid, String name) {
		PlayerBodies.render(mc, RENDER_TYPE, uuid, 0.4F);
		return FramebufferUtils.capture(RENDER_TARGET.get());
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(VidLibTextures.DEFAULT_PLAYER_BODY);
		}

		return get(mc, uuid).load(mc, false);
	}
}
