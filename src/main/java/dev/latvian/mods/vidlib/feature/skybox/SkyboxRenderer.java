package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Draws a VidLib skybox cube in place of the vanilla sky.
 * <p>
 * 26.3 removed {@code net.minecraft.client.renderer.MultiBufferSource} and the
 * {@code RenderBuffers#bufferSource()} accessor, so the cube is built into a
 * {@link BufferBuilder} and drawn with an immediate {@code RenderPass} on the main render target —
 * the same way {@code SkyRenderer} draws its own celestial buffers.
 */
public class SkyboxRenderer {
	public static boolean render(Minecraft mc, ClientSkybox skybox, Runnable setupFog) {
		if (setupFog != null) {
			setupFog.run();
		}

		float ps = (float) (mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.depthFar / 2D);
		float ns = -ps;

		var texture = skybox.loadTexture(mc);
		var renderType = VidLibRenderTypes.SKYBOX.apply(texture.resourceId());

		var ms = new PoseStack();
		ms.pushPose();

		if (skybox.data.rotating() != 0F) {
			float time = mc.level == null ? 0F : (mc.level.getGameTime() + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
			ms.mulPose(new Matrix4f().rotationY((float) Math.toRadians(skybox.data.rotation() + 360F * time * skybox.data.rotating())));
		}

		var m = ms.last();
		int cr = skybox.data.tint().red();
		int cg = skybox.data.tint().green();
		int cb = skybox.data.tint().blue();
		int ca = skybox.data.tint().alpha();

		try (var memory = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
			var bufferBuilder = new BufferBuilder(memory, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			buildFaces(bufferBuilder, m, ns, ps, cr, cg, cb, ca);

			try (MeshData mesh = bufferBuilder.buildOrThrow()) {
				var device = RenderSystem.getDevice();
				var target = mc.gameRenderer.mainRenderTarget();
				var quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
				GpuBuffer indexBuffer = quadIndices.getBuffer(24);

				try (
					GpuBuffer vertexBuffer = device.createBuffer(() -> "VidLib Skybox", 32, mesh.vertexBuffer());
					var renderPass = device.createCommandEncoder().createRenderPass(
						() -> "VidLib Skybox",
						target.getColorTextureView(),
						Optional.empty(),
						target.getDepthTextureView(),
						OptionalDouble.empty()
					)
				) {
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setPipeline(RenderSystem.getCompiledPipeline(renderType.pipeline()));
					renderPass.setUniform("Sampler0", texture.getTextureView(), texture.getSampler());
					renderPass.setVertexBuffer(0, vertexBuffer.slice());
					renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
					renderPass.drawIndexed(24, 1, 0, 0, 0);
				}
			}
		}

		ms.popPose();
		return !skybox.data.celestials();
	}

	private static void buildFaces(VertexConsumer buffer, PoseStack.Pose m, float ns, float ps, int cr, int cg, int cb, int ca) {
		float v0;
		float v1;
		float u0;
		float u1;

		// Up
		v0 = 0F;
		v1 = 0.5F;
		u0 = 0.25F;
		u1 = 0.5F;
		buffer.addVertex(m, ns, ps, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ps, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ns, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);

		// Down
		v0 = 0F;
		v1 = 0.5F;
		u0 = 0.5F;
		u1 = 0.75F;
		buffer.addVertex(m, ns, ns, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ns, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ns, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);

		v0 = 0.5F;
		v1 = 1F;

		// North
		u0 = 0.25F;
		u1 = 0.5F;
		buffer.addVertex(m, ns, ns, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ns, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ps, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ns, ps, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);

		// South
		u0 = 0.75F;
		u1 = 1F;
		buffer.addVertex(m, ns, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ns, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);

		// West
		u0 = 0F;
		u1 = 0.25F;
		buffer.addVertex(m, ns, ns, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ns, ps, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ns, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ns, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);

		// East
		u0 = 0.5F;
		u1 = 0.75F;
		buffer.addVertex(m, ps, ns, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
		buffer.addVertex(m, ps, ps, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
	}
}
