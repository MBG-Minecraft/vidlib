package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.FramebufferUtils;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.item.VisualItemKey;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public interface ItemIcons {
	@ClientAutoRegister
	Gallery<VisualItemKey> GALLERY = new Gallery<>("item_icons", () -> null, TriState.FALSE, VisualItemKey::toString, null);

	Lazy<RenderTarget> RENDER_TARGET = Lazy.of(() -> new TextureTarget("ItemIconsCanvas", 128, 128, true));

	TexturedRenderType RENDER_TYPE_CUTOUT = TexturedRenderType.internal(
		"item_icon/cutout",
		1536,
		true,
		true,
		RenderPipelines.ENTITY_CUTOUT,
		texture -> RenderType.CompositeState.builder()
			.setOutputState(new RenderStateShard.OutputStateShard("item_icon", RENDER_TARGET))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
	);

	TexturedRenderType RENDER_TYPE_TRANSLUCENT = TexturedRenderType.internal(
		"item_icon/translucent",
		1536,
		true,
		true,
		RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL,
		texture -> RenderType.CompositeState.builder()
			.setOutputState(new RenderStateShard.OutputStateShard("item_icon", RENDER_TARGET))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOverlayState(RenderStateShard.OVERLAY)
			.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
			.createCompositeState(true)
	);

	RenderType ORIGINAL_TRANSLUCENT_TYPE = RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);

	record BufferOverride(MultiBufferSource parent) implements MultiBufferSource {
		@Override
		public VertexConsumer getBuffer(RenderType renderType) {
			if (renderType == ORIGINAL_TRANSLUCENT_TYPE) {
				return parent.getBuffer(RENDER_TYPE_TRANSLUCENT.apply(renderType.vl$getTextureSafe()));
			} else {
				return parent.getBuffer(RENDER_TYPE_CUTOUT.apply(renderType.vl$getTextureSafe()));
			}
		}
	}

	Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.2F, 1F, 0.7F).normalize();
	Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.2F, 1F, -0.7F).normalize();
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
			var projectionMatrix = new Matrix4f(RenderSystem.getProjectionMatrix());
			var projectionType = RenderSystem.getProjectionType();

			var camera = new Matrix4f().setOrtho(-0.5F, 0.5F, 0.5F, -0.5F, 10F, -10F);
			camera.rotateZ((float) Math.PI);
			// camera.scale(-1F, -1F, 1F);
			RenderSystem.setProjectionMatrix(camera, ProjectionType.ORTHOGRAPHIC);
			Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushMatrix();
			modelViewStack.identity();

			try {
				var buffers = mc.renderBuffers().bufferSource();
				var scratchItemStackRenderState = new ItemStackRenderState();
				mc.getItemModelResolver().updateForTopItem(scratchItemStackRenderState, stack, ItemDisplayContext.GUI, null, null, 0);

				var pose = new PoseStack();
				pose.scale(1F, -1F, -1F);
				boolean flat = !scratchItemStackRenderState.usesBlockLight();

				if (flat) {
					var mat = new Matrix4f().rotationY((float) (-Math.PI / 8)).rotateY((float) Math.PI).rotateX((float) (Math.PI * 3.0 / 4.0));
					RenderSystem.setShaderLights(mat.transformDirection(DIFFUSE_LIGHT_0, new Vector3f()), mat.transformDirection(DIFFUSE_LIGHT_1, new Vector3f()));
				} else {
					RenderSystem.setupGui3DDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
				}

				scratchItemStackRenderState.render(pose, new BufferOverride(buffers), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
				RENDER_TYPE_CUTOUT.endBatches(buffers);
				RENDER_TYPE_TRANSLUCENT.endBatches(buffers);
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
				CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
				crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
				crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
				crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
				throw new ReportedException(crashreport);
			}

			Lighting.setupFor3DItems();
			modelViewStack.popMatrix();
			RenderSystem.setProjectionMatrix(projectionMatrix, projectionType);
		}
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable VisualItemKey key) {
		if (key == null || key == VisualItemKey.AIR) {
			return mc.getTextureManager().getTexture(VidLibTextures.TRANSPARENT);
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
