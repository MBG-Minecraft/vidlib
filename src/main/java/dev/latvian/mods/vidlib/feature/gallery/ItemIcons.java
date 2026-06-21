package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.FramebufferUtils;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.item.VisualItemKey;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;

public interface ItemIcons {
	@ClientAutoRegister
	Gallery<VisualItemKey> GALLERY = new Gallery<>("item_icons", VisualItemKey.CODEC, () -> null, TriState.FALSE, VisualItemKey::toString, null);

	Lazy<RenderTarget> RENDER_TARGET = Lazy.of(() -> new TextureTarget("ItemIconsCanvas", 128, 128, true));
	OutputTarget OUTPUT_TARGET = new OutputTarget("item_icon", RENDER_TARGET);

	TexturedRenderType RENDER_TYPE_CUTOUT = TexturedRenderType.internal(
		"item_icon/cutout",
		1536,
		true,
		true,
		texture -> TexturedRenderType.textured(RenderPipelines.ENTITY_CUTOUT, texture, true, true)
			.setOutputTarget(OUTPUT_TARGET)
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
	);

	TexturedRenderType RENDER_TYPE_TRANSLUCENT = TexturedRenderType.internal(
		"item_icon/translucent",
		1536,
		true,
		true,
		texture -> TexturedRenderType.textured(RenderPipelines.ENTITY_TRANSLUCENT_CULL, texture, true, true)
			.setOutputTarget(OUTPUT_TARGET)
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
	);

	RenderType ORIGINAL_TRANSLUCENT_TYPE = RenderTypes.entityTranslucentCullItemTarget(TextureAtlas.LOCATION_BLOCKS);

	ImagePreProcessor PRE_PROCESSOR = ImagePreProcessor.reduce(8, 8);

	static GalleryImage<VisualItemKey> get(Minecraft mc, VisualItemKey key) {
		return GALLERY.getRender(mc, key, k -> "", ItemIcons::render, PRE_PROCESSOR);
	}

	private static NativeImage render(Minecraft mc, VisualItemKey key, String name) {
		render(mc, key);
		return FramebufferUtils.capture(RENDER_TARGET.get(), 0, true, false);
	}

	static void render(Minecraft mc, VisualItemKey key) {
		var renderTarget = RENDER_TARGET.get();
		var gpu = RenderSystem.getDevice();
		gpu.createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1D);

		var stack = key.toItemStack();

		if (!stack.isEmpty()) {
			RenderSystem.backupProjectionMatrix();

			var camera = new Matrix4f().setOrtho(-0.5F, 0.5F, 0.5F, -0.5F, 10F, -10F);
			camera.rotateZ((float) Math.PI);
			// camera.scale(-1F, -1F, 1F);
			MiscClientUtils.setProjectionMatrix(camera, ProjectionType.ORTHOGRAPHIC);
			Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushMatrix();
			modelViewStack.identity();

			try {
				var scratchItemStackRenderState = new ItemStackRenderState();
				mc.getItemModelResolver().updateForTopItem(scratchItemStackRenderState, stack, ItemDisplayContext.GUI, null, null, 0);

				var pose = new PoseStack();
				pose.scale(1F, -1F, -1F);
				boolean flat = !scratchItemStackRenderState.usesBlockLight();
				mc.gameRenderer.getLighting().setupFor(flat ? Lighting.Entry.ITEMS_FLAT : Lighting.Entry.ITEMS_3D);

				var oldColorOverride = RenderSystem.outputColorTextureOverride;
				var oldDepthOverride = RenderSystem.outputDepthTextureOverride;
				RenderSystem.outputColorTextureOverride = renderTarget.getColorTextureView();
				RenderSystem.outputDepthTextureOverride = renderTarget.getDepthTextureView();

				try {
					var featureRenderDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
					scratchItemStackRenderState.submit(pose, featureRenderDispatcher.getSubmitNodeStorage(), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
					featureRenderDispatcher.renderAllFeatures();
				} finally {
					RenderSystem.outputColorTextureOverride = oldColorOverride;
					RenderSystem.outputDepthTextureOverride = oldDepthOverride;
				}
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
				CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
				crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
				crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
				crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
				throw new ReportedException(crashreport);
			}

			mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
			modelViewStack.popMatrix();
			RenderSystem.restoreProjectionMatrix();
		}
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable VisualItemKey key) {
		if (key == null || key == VisualItemKey.AIR) {
			return mc.getTextureManager().getTexture(VidLibTextures.TRANSPARENT.texturePath());
		}

		var tex = get(mc, key).load(mc, false);
		var blur = tex.getTexture().getWidth(0) >= 64;
		GlStateManager._bindTexture(tex.getTexture().vl$getHandle());
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		GlStateManager._bindTexture(0);
		return tex;
	}
}
