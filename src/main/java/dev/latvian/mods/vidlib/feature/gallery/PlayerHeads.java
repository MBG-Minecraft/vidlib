package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.klib.util.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import java.util.UUID;

public interface PlayerHeads {
	int SIZE = 256;

	@ClientAutoRegister
	Gallery<UUID> GALLERY_NO_LAYERS = Gallery.ofUUIDKey("player_heads_no_layers", () -> VidLibPaths.USER.get().resolve("player-heads-no-layers"), TriState.TRUE);

	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("player_heads", () -> VidLibPaths.USER.get().resolve("player-heads"), TriState.TRUE).addUploader(new GalleryUploader<>() {
		public static final GameProfileImBuilder UNIT = new GameProfileImBuilder();

		@Override
		public Identifier getIcon() {
			return VidLibTextures.DEFAULT_PLAYER_HEAD.id();
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

	Lazy<RenderTarget> RENDER_TARGET = Lazy.of(() -> new TextureTarget("PlayerHeadsCanvas", SIZE, SIZE, GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT));

	TexturedRenderType RENDER_TYPE = TexturedRenderType.internal("player_head", RenderPipelines.ENTITY_CUTOUT, texture -> RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
		.withTexture("Sampler0", texture)
		.useLightmap()
		.useOverlay()
		.createRenderSetup());

	static GalleryImage<UUID> get(Minecraft mc, UUID uuid) {
		return get(GALLERY, mc, uuid, true, null);
	}

	static GalleryImage<UUID> getNoLayers(Minecraft mc, UUID uuid) {
		return get(GALLERY_NO_LAYERS, mc, uuid, false, null);
	}

	static GalleryImage<UUID> refresh(Minecraft mc, UUID uuid, PlayerSkin skin) {
		return get(GALLERY, mc, uuid, true, skin);
	}

	static GalleryImage<UUID> refreshNoLayers(Minecraft mc, UUID uuid, PlayerSkin skin) {
		return get(GALLERY_NO_LAYERS, mc, uuid, false, skin);
	}

	private static GalleryImage<UUID> get(Gallery<UUID> gallery, Minecraft mc, UUID uuid, boolean layers, @Nullable PlayerSkin skinOrNull) {
		if (skinOrNull != null) {
			var old = gallery.images.remove(uuid);

			if (old != null && mc.getTextureManager().byPath.remove(old.textureId()) instanceof DynamicTexture tex) {
				tex.close();
			}
		}

		return gallery.getRender(mc, uuid, PlayerProfiles::getName, (m, u, n) -> render(m, u, n, layers, skinOrNull != null ? skinOrNull : PlayerSkins.getSkin(m, u, true)), ImagePreProcessor.NONE);
	}

	private static NativeImage render(Minecraft mc, UUID uuid, String name, boolean layers, PlayerSkin skin) {
		return OffscreenRenderer.image(SIZE, SIZE, true, offscreenPose(layers), offscreen -> submitHead(offscreen, mc, skin, layers));
	}

	static void render(Minecraft mc, UUID uuid, float zoom) {
		render(mc, uuid, zoom, true);
	}

	static void render(Minecraft mc, UUID uuid, float zoom, boolean layers) {
		render(mc, PlayerSkins.getSkin(mc, uuid, true), zoom, layers);
	}

	static void render(Minecraft mc, PlayerSkin playerSkin, float zoom) {
		render(mc, playerSkin, zoom, true);
	}

	static void render(Minecraft mc, PlayerSkin playerSkin, float zoom, boolean layers) {
		var projectionMatrix = RenderSystem.getProjectionMatrixBuffer();
		var projectionType = RenderSystem.getProjectionType();
		var camera = new Matrix4f().setOrtho(-zoom, zoom, zoom, -zoom, -10F, 10F);
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		modelViewStack.rotate(new Quaternionf().rotationXYZ((float) Math.toRadians(25D), (float) Math.toRadians(45D), (float) Math.PI));

		try {
			OffscreenRenderer.render(SIZE, SIZE, true, offscreenPose(layers), offscreen -> submitHead(offscreen, mc, playerSkin, layers), target -> {
				RenderSystem.setProjectionMatrix(RenderSystem.getDynamicUniforms().writeTransform(camera), com.mojang.blaze3d.ProjectionType.ORTHOGRAPHIC);
				return null;
			});
		} finally {
			modelViewStack.popMatrix();
			RenderSystem.setProjectionMatrix(projectionMatrix, projectionType);
		}
	}

	private static java.util.function.Consumer<PoseStack> offscreenPose(boolean layers) {
		return ms -> {
			ms.scale(-1F, 1F, -1F);
			ms.translate(0F, 0.25F, 0F);
		};
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void submitHead(OffscreenRenderer offscreen, Minecraft mc, PlayerSkin playerSkin, boolean layers) {
		var state = new AvatarRenderState();
		state.skin = playerSkin;
		state.showHat = layers;

		AvatarRenderer renderer = (AvatarRenderer) mc.getEntityRenderDispatcher().getRenderer(state);
		var model = (net.minecraft.client.model.player.PlayerModel) renderer.getModel();
		model.setupAnim(state);
		model.head.visible = true;
		model.hat.visible = layers;

		var ms = new PoseStack();
		ms.scale(-1F, 1F, -1F);
		ms.translate(0F, 0.25F, 0F);
		// FIXME renderer.submit(state, ms, offscreen, new CameraRenderState());
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(VidLibTextures.DEFAULT_PLAYER_HEAD.id());
		}

		return get(mc, uuid).load(mc, false);
	}
}
