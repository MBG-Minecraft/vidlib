package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import dev.latvian.mods.vidlib.feature.client.VLRenderPipelines;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.ARGB;

public class SkyboxRenderer {
	public static final UV[] UVS = new UV[6];

	static {
		UVS[0] = new UV(0.5F, 0F, 0.75F, 0.5F);
		UVS[1] = new UV(0.25F, 0F, 0.5F, 0.5F);
		UVS[2] = new UV(0.25F, 0.5F, 0.5F, 1F);
		UVS[3] = new UV(0.75F, 0.5F, 1F, 1F);
		UVS[4] = new UV(0F, 0.5F, 0.25F, 1F);
		UVS[5] = new UV(0.5F, 0.5F, 0.75F, 1F);
	}

	public static GpuBuffer skyBuffer;

	public static void open() {
		try (var byteBufferBuilder = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
			var buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			float ps = 100F;
			float ns = -ps;

			// Up
			{
				float u0 = 0.25F;
				float v0 = 0F;
				float u1 = 0.5F;
				float v1 = 0.5F;
				buffer.addVertex(ns, ps, ns).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ns).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ps).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ps).setUv(u0, v0).setColor(0xFFFFFFFF);
			}

			// Down
			{
				float u0 = 0.5F;
				float v0 = 0F;
				float u1 = 0.75F;
				float v1 = 0.5F;
				buffer.addVertex(ns, ns, ns).setUv(u0, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ns, ps).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ps).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ns).setUv(u1, v0).setColor(0xFFFFFFFF);
			}

			float v0 = 0.5F;
			float v1 = 1F;

			// North
			{
				float u0 = 0.25F;
				float u1 = 0.5F;
				buffer.addVertex(ns, ns, ns).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ns).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ns).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ns).setUv(u0, v0).setColor(0xFFFFFFFF);
			}

			// South
			{
				float u0 = 0.75F;
				float u1 = 1F;
				buffer.addVertex(ns, ns, ps).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ps).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ps).setUv(u0, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ps).setUv(u0, v1).setColor(0xFFFFFFFF);
			}

			// West
			{
				float u0 = 0F;
				float u1 = 0.25F;
				buffer.addVertex(ns, ns, ns).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ns).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ps).setUv(u0, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ns, ps).setUv(u0, v1).setColor(0xFFFFFFFF);
			}

			// East
			{
				float u0 = 0.5F;
				float u1 = 0.75F;
				buffer.addVertex(ps, ns, ns).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ps).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ps).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ns).setUv(u0, v0).setColor(0xFFFFFFFF);
			}

			try (var meshData = buffer.buildOrThrow()) {
				skyBuffer = RenderSystem.getDevice().createBuffer(() -> "VidLib Skybox vertex buffer", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());
			}
		}
	}

	public static boolean render(RenderPass renderPass, PoseStack poseStack, SkyRenderState skyState) {
		var vl = ((VLSkyRenderState) skyState).vl$extra();

		if (vl.skyboxTexture == null) {
			return false;
		}

		skyState.shouldRenderDarkDisc = false;
		skyState.sunriseAndSunsetColor = ARGB.vector4fFromARGB32(0);


		var autoIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		var indexBuffer = autoIndices.getBuffer(36);
		var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());
		renderPass.pushDebugGroup(() -> "VidLib Skybox");
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(VLRenderPipelines.SKYBOX));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", dynamicTransforms);
		renderPass.setUniform("Sampler0", vl.skyboxTexture.getTextureView(), vl.skyboxTexture.getSampler());
		renderPass.setVertexBuffer(0, skyBuffer.slice());
		renderPass.setIndexBuffer(indexBuffer, autoIndices.type());
		renderPass.drawIndexed(36, 1, 0, 0, 0);
		renderPass.popDebugGroup();
		return true;
	}

	public static void close() {
		if (skyBuffer != null) {
			skyBuffer.close();
			skyBuffer = null;
		}
	}
}
