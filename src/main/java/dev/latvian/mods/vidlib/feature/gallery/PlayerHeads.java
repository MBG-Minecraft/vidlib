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
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import imgui.ImGui;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import java.util.UUID;

public interface PlayerHeads {
	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("player_heads", () -> VidLibPaths.USER.get().resolve("player-heads"), TriState.TRUE).addUploader(new GalleryUploader<>() {
		public static final GameProfileImBuilder UNIT = new GameProfileImBuilder();

		@Override
		public Identifier getIcon() {
			return VidLibTextures.DEFAULT_PLAYER_HEAD.texturePath();
		}

		@Override
		public String getTooltip() {
			return "Select a Head Icon";
		}

		@Override
		public ImColorVariant getColor() {
			return ImColorVariant.BLUE;
		}

		@Override
		public void render(Gallery<UUID> gallery, GalleryImageImBuilder builder, ImGraphics graphics, boolean clicked) {
			if (clicked) {
				ImGui.openPopup("###select-profile");
			}

			if (ImGui.beginPopup("###select-profile")) {
				if (UNIT.profileSelector(graphics, profile -> !profile.equals(PlayerProfile.EMPTY_GAME_PROFILE) && !GALLERY.images.containsKey(profile.id())).isFull() && UNIT.isValid()) {
					var profile = UNIT.build();

					if (profile != null && !PlayerProfile.EMPTY_GAME_PROFILE.equals(profile)) {
						try {
							builder.set(get(graphics.mc, profile.id()));
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
	});

	Lazy<RenderTarget> RENDER_TARGET = Lazy.of(() -> new TextureTarget("PlayerHeadsCanvas", 1024, 1024, true));
	OutputTarget OUTPUT_TARGET = new OutputTarget("player_head", RENDER_TARGET);

	TexturedRenderType RENDER_TYPE = TexturedRenderType.internal(
		"player_head",
		1536,
		true,
		true,
		texture -> TexturedRenderType.textured(RenderPipelines.ENTITY_CUTOUT, texture, true, true)
			.setOutputTarget(OUTPUT_TARGET)
			.setOutline(RenderSetup.OutlineProperty.NONE)
	);

	static GalleryImage<UUID> get(Minecraft mc, UUID uuid) {
		return GALLERY.getRender(mc, uuid, PlayerProfiles::getName, PlayerHeads::render, ImagePreProcessor.NONE);
	}

	private static NativeImage render(Minecraft mc, UUID uuid, String name) {
		render(mc, RENDER_TYPE, uuid, 0.45F);
		return FramebufferUtils.capture(RENDER_TARGET.get());
	}

	static void render(Minecraft mc, TexturedRenderType type, UUID uuid, float zoom) {
		var gpu = RenderSystem.getDevice();

		RenderSystem.backupProjectionMatrix();
		MiscClientUtils.setProjectionMatrix(new Matrix4f().setOrtho(-zoom, zoom, zoom, -zoom, -10F, 10F), ProjectionType.ORTHOGRAPHIC);
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		// modelViewStack.translate(0F, 0F, 0F);
		modelViewStack.rotate(new Quaternionf().rotationXYZ((float) Math.toRadians(25D), (float) Math.toRadians(45D), (float) Math.PI));
		// modelViewStack.scale(1F, 1F, -1F);
		mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);

		var playerSkin = PlayerSkins.getSkin(mc, uuid, true);
		var buffers = mc.renderBuffers().bufferSource();
		var playerRenderState = new AvatarRenderState();
		playerRenderState.skin = playerSkin;
		var playerRenderer = (AvatarRenderer<?>) mc.getEntityRenderDispatcher().getRenderer(playerRenderState);

		var renderType = type.apply(playerRenderer.getTextureLocation(playerRenderState));
		var renderTarget = renderType.outputTarget().getRenderTarget();
		gpu.createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1D);

		var buffer = buffers.getBuffer(renderType);
		var model = playerRenderer.getModel();

		playerRenderState.showHat = true;
		playerRenderState.showJacket = false;
		playerRenderState.showLeftPants = false;
		playerRenderState.showRightPants = false;
		playerRenderState.showLeftSleeve = false;
		playerRenderState.showRightSleeve = false;
		model.setupAnim(playerRenderState);
		model.body.visible = false;
		model.leftArm.visible = false;
		model.rightArm.visible = false;
		model.leftLeg.visible = false;
		model.rightLeg.visible = false;
		model.head.visible = true;
		model.hat.visible = true;

		var ms = new PoseStack();
		ms.scale(-1F, 1F, -1F);
		ms.translate(0F, 0.25F, 0F);
		model.renderToBuffer(ms, buffer, LightUV.FULLBRIGHT.packed(), OverlayUV.NORMAL.packed());
		buffers.endBatch(renderType);

		modelViewStack.popMatrix();
		RenderSystem.restoreProjectionMatrix();
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(VidLibTextures.DEFAULT_PLAYER_HEAD.texturePath());
		}

		return get(mc, uuid).load(mc, false);
	}
}
