package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import dev.latvian.mods.klib.gl.StaticBuffers;
import dev.latvian.mods.klib.util.client.FramebufferUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;

/**
 * Offscreen renderer used by the icon galleries.
 * <p>
 * 26.3 replaced {@code MultiBufferSource}-driven rendering with the {@link SubmitNodeCollector}
 * submit model. This bridge accepts the same submit calls and replays them into a plain
 * {@link BufferBuilder} per {@link RenderType}, which is then uploaded and drawn into a dedicated
 * {@link RenderTarget} so the result can be captured into a {@link com.mojang.blaze3d.platform.NativeImage}.
 */
public class OffscreenRenderer implements SubmitNodeCollector, AutoCloseable {
	private static final int BUFFER_SIZE = 65536;

	private final class Batch {
		private final ByteBufferBuilder storage = new ByteBufferBuilder(BUFFER_SIZE);
		private final BufferBuilder builder;
		private final VertexElementFiller consumer;

		private Batch(RenderType renderType) {
			this.builder = new BufferBuilder(this.storage, PrimitiveTopology.QUADS, renderType.format());
			this.consumer = new VertexElementFiller(this.builder);
		}

		private MeshData build() {
			this.consumer.finish();
			return this.builder.build();
		}
	}

	private final Map<RenderType, Batch> batches = new IdentityHashMap<>();

	public VertexConsumer vertexConsumer(RenderType renderType) {
		return batches.computeIfAbsent(renderType, Batch::new).consumer;
	}

	@Override
	public OrderedSubmitNodeCollector order(int order) {
		return this;
	}

	@Override
	public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer customGeometryRenderer) {
		customGeometryRenderer.render(poseStack.last(), vertexConsumer(renderType));
	}

	@Override
	public <S> void submitModel(
		Model<? super S> model,
		S state,
		PoseStack poseStack,
		RenderType renderType,
		int lightCoords,
		int overlayCoords,
		int tintedColor,
		@Nullable UvMapping uvMapping,
		int outlineColor
	) {
		if (uvMapping != null) {
			// UvMapping support was removed together with the old render-type pipeline.
			return;
		}

		model.renderToBuffer(poseStack, vertexConsumer(renderType), lightCoords, overlayCoords, tintedColor);
	}

	@Override
	public void submitItem(
		PoseStack poseStack,
		ItemDisplayContext displayContext,
		int lightCoords,
		int overlayCoords,
		int outlineColor,
		int[] tintLayers,
		ItemQuads quads,
		ItemStackRenderState.FoilType foilType
	) {
		var instance = new QuadInstance();

		for (int vertex = 0; vertex < 4; vertex++) {
			instance.setColor(vertex, vertex < tintLayers.length ? tintLayers[vertex] : -1);
			instance.setLightCoords(vertex, lightCoords);
		}

		instance.setOverlayCoords(overlayCoords);

		for (BakedQuad quad : quads.all()) {
			var type = quad.materialInfo().itemRenderType();
			var consumer = vertexConsumer(type);
			var pose = poseStack.last();
			float x = pose.pose().m30();
			float y = pose.pose().m31();
			float z = pose.pose().m32();
			consumer.putBlockBakedQuad(x, y, z, quad, instance);
		}
	}

	private void draw(RenderTarget target) {
		var device = RenderSystem.getDevice();

		for (var entry : batches.entrySet()) {
			var type = entry.getKey();

			try (var meshData = entry.getValue().build()) {
				if (meshData == null) {
					continue;
				}

				var buffers = StaticBuffers.of(meshData, () -> "Gallery");
				var pipeline = type.pipeline();

				try (var renderPass = device.createCommandEncoder().createRenderPass(
					() -> "Gallery",
					target.getColorTextureView(),
					Optional.empty(),
					target.getDepthTextureView(),
					OptionalDouble.of(1D)
				)) {
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
					buffers.setIndexBuffer(renderPass, pipeline);
					renderPass.setVertexBuffer(0, buffers.vertexBuffer().slice());
					renderPass.drawIndexed(buffers.indexCount(), 1, 0, 0, 0);
				}
			}
		}

		close();
	}

	/**
	 * Renders into a fresh {@link RenderTarget} and returns the captured image.
	 */
	public static <T> T render(
		int width,
		int height,
		boolean useDepth,
		Consumer<PoseStack> poseSetup,
		Consumer<OffscreenRenderer> body,
		java.util.function.Function<RenderTarget, T> capture
	) {
		var target = new TextureTarget("VidLibGalleryCanvas", width, height, com.mojang.renderpearl.api.GpuFormat.RGBA8_UNORM, useDepth ? com.mojang.renderpearl.api.GpuFormat.D32_FLOAT : null);

		try {
			var renderer = new OffscreenRenderer();
			var poseStack = new PoseStack();
			poseSetup.accept(poseStack);
			body.accept(renderer);
			renderer.draw(target);
			return capture.apply(target);
		} finally {
			target.destroyBuffers();
		}
	}

	public static com.mojang.blaze3d.platform.NativeImage image(int width, int height, boolean useDepth, Consumer<PoseStack> poseSetup, Consumer<OffscreenRenderer> body) {
		return render(width, height, useDepth, poseSetup, body, FramebufferUtils::capture);
	}

	@Override
	public void close() {
		batches.clear();
	}

	// --- unused submit entry points -------------------------------------------------

	@Override
	public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
	}

	@Override
	public void submitNameTag(
		PoseStack poseStack,
		@Nullable Vec3 nameTagAttachment,
		int offset,
		Component name,
		boolean seeThrough,
		int lightCoords,
		CameraRenderState camera
	) {
	}

	@Override
	public void submitText(
		PoseStack poseStack,
		float x,
		float y,
		FormattedCharSequence string,
		boolean dropShadow,
		Font.DisplayMode displayMode,
		int lightCoords,
		int color,
		int backgroundColor,
		int outlineColor
	) {
	}

	@Override
	public void submitTextBackground(PoseStack poseStack, float x0, float y0, float x1, float y1, int color, Font.DisplayMode displayMode, int lightCoords) {
	}

	@Override
	public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
	}

	@Override
	public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
	}

	@Override
	public <S> void submitCrumblingOverlay(
		Model<? super S> model,
		S state,
		PoseStack poseStack,
		RenderType renderType,
		int lightCoords,
		int overlayCoords,
		int tintedColor,
		ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
	}

	@Override
	public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState, int outlineColor) {
	}

	@Override
	public void submitBlockModel(
		PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor
	) {
	}

	@Override
	public void submitBreakingBlockModel(PoseStack poseStack, List<BlockStateModelPart> parts, int progress, boolean isBlockTranslucent) {
	}

	@Override
	public void submitShapeOutline(PoseStack poseStack, VoxelShape shape, RenderType renderType, int color, float width, boolean afterTerrain) {
	}

	@Override
	public void submitQuadParticleGroup(QuadParticleRenderState particles) {
	}

	@Override
	public void submitGizmoPrimitives(DrawableGizmoPrimitives.Group group, CameraRenderState camera, boolean onTop) {
	}
}
