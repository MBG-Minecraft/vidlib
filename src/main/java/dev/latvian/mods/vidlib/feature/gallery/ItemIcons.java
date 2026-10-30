package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.klib.util.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.item.VisualItemKey;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public interface ItemIcons {
	int SIZE = 128;

	@ClientAutoRegister
	Gallery<VisualItemKey> GALLERY = new Gallery<>("item_icons", VisualItemKey.CODEC, () -> null, net.minecraft.util.TriState.FALSE, VisualItemKey::toString, null);

	Lazy<RenderTarget> RENDER_TARGET = Lazy.of(() -> new TextureTarget("ItemIconsCanvas", SIZE, SIZE, GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT));

	TexturedRenderType RENDER_TYPE_CUTOUT = TexturedRenderType.internal("item_icon/cutout", net.minecraft.client.renderer.RenderPipelines.ENTITY_CUTOUT, texture -> RenderSetup.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_CUTOUT)
		.useLightmap()
		.useOverlay()
		.withTexture("Sampler0", texture)
		.createRenderSetup());

	TexturedRenderType RENDER_TYPE_TRANSLUCENT = TexturedRenderType.internal("item_icon/translucent", net.minecraft.client.renderer.RenderPipelines.ENTITY_TRANSLUCENT_CULL, texture -> RenderSetup.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_TRANSLUCENT_CULL)
		.useLightmap()
		.useOverlay()
		.withTexture("Sampler0", texture)
		.createRenderSetup());

	RenderType ORIGINAL_TRANSLUCENT_TYPE = RenderTypes.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);

	record BufferOverride(OffscreenRenderer parent) {
	}

	Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.2F, 1F, 0.7F).normalize();
	Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.2F, 1F, -0.7F).normalize();
	ImagePreProcessor PRE_PROCESSOR = ImagePreProcessor.reduce(8, 8);

	static GalleryImage<VisualItemKey> get(Minecraft mc, VisualItemKey key) {
		return GALLERY.getRender(mc, key, k -> "", ItemIcons::render, PRE_PROCESSOR);
	}

	private static NativeImage render(Minecraft mc, VisualItemKey key, String name) {
		render(mc, key);
		return OffscreenRenderer.image(SIZE, SIZE, true, ms -> ms.scale(1F, -1F, -1F), offscreen -> submitItem(offscreen, mc, key));
	}

	static void render(Minecraft mc, VisualItemKey key) {
		var stack = key.toItemStack();

		if (!stack.isEmpty()) {
			var projectionMatrix = RenderSystem.getProjectionMatrixBuffer();
			var projectionType = RenderSystem.getProjectionType();
			var camera = new Matrix4f().setOrtho(-0.5F, 0.5F, 0.5F, -0.5F, 10F, -10F);
			camera.rotateZ((float) Math.PI);
			var cameraSlice = RenderSystem.getDynamicUniforms().writeTransform(camera);

			Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushMatrix();
			modelViewStack.identity();

			try {
				OffscreenRenderer.render(SIZE, SIZE, true, ms -> ms.scale(1F, -1F, -1F), offscreen -> submitItem(offscreen, mc, key), target -> {
					RenderSystem.setProjectionMatrix(cameraSlice, ProjectionType.ORTHOGRAPHIC);
					return null;
				});
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
				CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
				crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
				crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
				crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
				throw new ReportedException(crashreport);
			}

			modelViewStack.popMatrix();
			RenderSystem.setProjectionMatrix(projectionMatrix, projectionType);
		}
	}

	private static void submitItem(OffscreenRenderer offscreen, Minecraft mc, VisualItemKey key) {
		var stack = key.toItemStack();
		var scratchItemStackRenderState = new ItemStackRenderState();
		mc.getItemModelResolver().updateForTopItem(scratchItemStackRenderState, stack, ItemDisplayContext.GUI, null, null, 0);

		var pose = new PoseStack();
		pose.scale(1F, -1F, -1F);
		boolean flat = !scratchItemStackRenderState.usesBlockLight();

		// 26.3 moved per-entry diffuse lighting into the shared Lighting UBO.
		var lighting = flat ? com.mojang.blaze3d.platform.Lighting.Entry.ITEMS_FLAT : com.mojang.blaze3d.platform.Lighting.Entry.ITEMS_3D;
		Minecraft.getInstance().gameRenderer.lighting().setupFor(lighting);

		scratchItemStackRenderState.submit(pose, offscreen, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable VisualItemKey key) {
		if (key == null || key == VisualItemKey.AIR) {
			return mc.getTextureManager().getTexture(VidLibTextures.TRANSPARENT.id());
		}

		var tex = get(mc, key).load(mc, false);
		var blur = tex.getTexture().getWidth(0) >= 64;
		GlStateManager._bindTexture(((GlTexture) tex.getTexture()).glId());
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		GlStateManager._bindTexture(0);
		return tex;
	}
}
