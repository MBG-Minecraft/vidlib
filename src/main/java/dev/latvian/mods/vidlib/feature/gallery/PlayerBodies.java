package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.klib.texture.OverlayUV;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.FramebufferUtils;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import imgui.ImGui;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import java.util.UUID;

public interface PlayerBodies {
	@ClientAutoRegister
	Gallery GALLERY = new Gallery("player_bodies", () -> VidLibPaths.USER.get().resolve("player-bodies"), TriState.TRUE);

	Lazy<RenderTarget> RENDER_TARGET = Lazy.of(() -> new TextureTarget("PlayerBodiesCanvas", 512, 512, true));

	TexturedRenderType RENDER_TYPE = TexturedRenderType.internal(
		"player_body",
		1536,
		true,
		true,
		RenderPipelines.ENTITY_CUTOUT_NO_CULL,
		texture -> RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.setOutputState(new RenderStateShard.OutputStateShard("player_body", RENDER_TARGET))
			.createCompositeState(false)
	);

	GalleryImageImBuilder.Uploader UPLOADER = new GalleryImageImBuilder.Uploader() {
		public static final GameProfileImBuilder UNIT = new GameProfileImBuilder();

		@Override
		public ResourceLocation getIcon() {
			return VidLibTextures.ID_CARD;
		}

		@Override
		public String getTooltip() {
			return "Fetch...";
		}

		@Override
		public void render(GalleryImageImBuilder builder, ImGraphics graphics, boolean clicked) {
			if (clicked) {
				ImGui.openPopup("###select-profile");
			}

			if (ImGui.beginPopup("###select-profile")) {
				if (UNIT.profileSelector(graphics, profile -> !profile.equals(PlayerProfile.EMPTY_GAME_PROFILE) && !GALLERY.images.containsKey(profile.getId())).isFull() && UNIT.isValid()) {
					var profile = UNIT.build();

					if (profile != null && !PlayerProfile.EMPTY_GAME_PROFILE.equals(profile)) {
						try {
							builder.set(get(graphics.mc, profile.getId()));
							builder.fullUpdate = true;
							ImGui.closeCurrentPopup();
						} catch (Exception ex) {
							throw new RuntimeException(ex);
						}
					}
				}

				ImGui.endPopup();
			}
		}
	};

	static GalleryImage get(Minecraft mc, UUID uuid) {
		return GALLERY.getRender(mc, uuid, PlayerProfiles::getName, PlayerBodies::render, ImagePreProcessor.NONE);
	}

	private static NativeImage render(Minecraft mc, UUID uuid, String name) {
		render(mc, RENDER_TYPE, uuid, 0.45F);
		return FramebufferUtils.capture(RENDER_TARGET.get(), 0);
	}

	static void render(Minecraft mc, TexturedRenderType type, UUID uuid, float zoom) {
		var gpu = RenderSystem.getDevice();

		var projectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
		var projectionType = RenderSystem.getProjectionType();
		RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-zoom, zoom, zoom, -zoom, -10F, 10F), ProjectionType.ORTHOGRAPHIC);
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		// modelViewStack.translate(0F, 0F, 0F);
		modelViewStack.rotate(new Quaternionf().rotationXYZ((float) Math.toRadians(10D), (float) Math.toRadians(25D), (float) Math.PI));
		// modelViewStack.scale(1F, 1F, -1F);
		Lighting.setupForEntityInInventory();

		var buffers = mc.renderBuffers().bufferSource();
		var profile = PlayerProfiles.get(uuid);
		var modelType = mc.getModelType(profile);
		var playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getSkinMap().get(modelType);
		var playerRenderState = playerRenderer.createRenderState();

		var skin = PlayerSkins.get(mc, uuid);
		skin.load(mc, true);
		playerRenderState.skin = new PlayerSkin(skin.textureId(), profile.skinUrl().orElse(null), null, null, modelType, true);
		var renderType = type.apply(playerRenderer.getTextureLocation(playerRenderState));
		var renderTarget = renderType.getRenderTarget();
		gpu.createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1D);

		var buffer = buffers.getBuffer(renderType);
		var model = playerRenderer.getModel();

		model.setAllVisible(true);
		playerRenderState.showHat = true;
		playerRenderState.showJacket = true;
		playerRenderState.showLeftPants = false;
		playerRenderState.showRightPants = false;
		playerRenderState.showLeftSleeve = true;
		playerRenderState.showRightSleeve = true;
		model.setupAnim(playerRenderState);
		model.leftLeg.visible = false;
		model.rightLeg.visible = false;

		var ms = new PoseStack();
		ms.scale(-1F, 1F, -1F);
		ms.translate(0F, 0.25F, 0F);
		model.renderToBuffer(ms, buffer, LightUV.FULLBRIGHT.packed(), OverlayUV.NORMAL.packed());
		playerRenderer.render(playerRenderState, ms, buffers, LightUV.FULLBRIGHT.packed());
		buffers.endBatch(renderType);

		modelViewStack.popMatrix();
		RenderSystem.setProjectionMatrix(projectionMatrix, projectionType);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(VidLibTextures.DEFAULT_PLAYER_BODY);
		}

		return get(mc, uuid).load(mc, false);
	}
}
