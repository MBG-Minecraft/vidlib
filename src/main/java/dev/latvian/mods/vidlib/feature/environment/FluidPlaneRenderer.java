package dev.latvian.mods.vidlib.feature.environment;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.klib.gl.StaticBuffers;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.TerrainRenderTypes;
import dev.latvian.mods.vidlib.feature.visual.DynamicSpriteTexture;
import dev.latvian.mods.vidlib.feature.visual.ResolvedCubeTextures;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class FluidPlaneRenderer {
	public static final Map<ResourceLocation, StaticBuffers> BUFFERS = new HashMap<>();

	@AutoInit({AutoInit.Type.TEXTURES_RELOADED, AutoInit.Type.CHUNKS_RENDERED})
	public static void refreshBuffers() {
		for (var buffer : BUFFERS.values()) {
			buffer.close();
		}

		BUFFERS.clear();
	}

	private static void appendPlane(BufferBuilder builder, float minX, float minZ, float maxX, float maxZ, int colR, int colG, int colB, int colA, int lu, int lv) {
		builder.addVertex(minX, 0F, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(0F, 1F, 0F);
		builder.addVertex(minX, 0F, maxZ).setColor(colR, colG, colB, colA).setUv(0F, maxZ - minZ).setUv2(lu, lv).setNormal(0F, 1F, 0F);
		builder.addVertex(maxX, 0F, maxZ).setColor(colR, colG, colB, colA).setUv(maxX - minX, maxZ - minZ).setUv2(lu, lv).setNormal(0F, 1F, 0F);
		builder.addVertex(maxX, 0F, minZ).setColor(colR, colG, colB, colA).setUv(maxX - minX, 0F).setUv2(lu, lv).setNormal(0F, 1F, 0F);
	}

	public static void render(FrameInfo frame, FluidPlane fluidPlane) {
		var tex = ResolvedCubeTextures.resolve(fluidPlane.fluid().textures().cubeTextures());

		if (tex.anyIn(frame.layer())) {
			var tex1 = tex.filter(frame.layer(), true);

			if (!tex1.isEmpty()) {
				int r = 256;
				float s = 8192F;
				double x = Mth.lfloor(frame.cameraX());
				double y = fluidPlane.y();
				double z = Mth.lfloor(frame.cameraZ());
				var tint = fluidPlane.fluid().textures().tint();
				var light = LightUV.FULLBRIGHT;
				int colR = tint.red();
				int colG = tint.green();
				int colB = tint.blue();
				int colA = tint.alpha();
				int lu = light.u();
				int lv = light.v();
				var texture = fluidPlane.fluid().textures().still();

				var buffers = BUFFERS.get(texture);

				if (buffers == null || !buffers.isEmpty() && buffers.vertexBuffer().isClosed()) {
					var format = DefaultVertexFormat.BLOCK;
					buffers = StaticBuffers.empty(format);

					try (var memory = new ByteBufferBuilder(format.getVertexSize() * 4 * (r * r + 4))) {
						var bufferBuilder = new BufferBuilder(memory, VertexFormat.Mode.QUADS, format);

						appendPlane(bufferBuilder, -s, -r, -r, r + 1F, colR, colG, colB, colA, lu, lv); // Left
						appendPlane(bufferBuilder, r + 1F, -r, s + 1F, r + 1F, colR, colG, colB, colA, lu, lv); // Right
						appendPlane(bufferBuilder, -s, -s, s + 1F, -r, colR, colG, colB, colA, lu, lv); // Top
						appendPlane(bufferBuilder, -s, r + 1F, s + 1F, s + 1F, colR, colG, colB, colA, lu, lv); // Bottom

						for (int pz = 0; pz < r * 2 + 1; pz++) {
							for (int px = 0; px < r * 2 + 1; px++) {
								float minX = px - r;
								float minZ = pz - r;
								float maxX = minX + 1F;
								float maxZ = minZ + 1F;
								appendPlane(bufferBuilder, minX, minZ, maxX, maxZ, colR, colG, colB, colA, lu, lv);
							}
						}

						try (var meshData = bufferBuilder.build()) {
							if (meshData != null) {
								buffers = StaticBuffers.of(meshData, () -> "Fluid Plane Buffer");

								var replaced = BUFFERS.put(texture, buffers);

								if (replaced != null) {
									replaced.close();
								}
							}
						}
					}
				}

				var renderTexture = DynamicSpriteTexture.get(tex1.faces().get(1).sprite());
				var renderType = TerrainRenderTypes.get(frame.layer(), false).apply(renderTexture);

				var modelViewMatrix = RenderSystem.getModelViewStack();
				modelViewMatrix.pushMatrix();
				modelViewMatrix.translate(
					frame.x(x),
					frame.y(y),
					frame.z(z)
				);

				renderType.setupRenderState();

				var renderTarget = renderType.getRenderTarget();

				try (var renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(
						renderTarget.getColorTexture(),
						OptionalInt.empty(),
						renderTarget.useDepth ? renderTarget.getDepthTexture() : null,
						OptionalDouble.empty()
					)
				) {
					renderPass.setPipeline(renderType.getRenderPipeline());
					renderPass.bindSampler("Sampler0", RenderSystem.getShaderTexture(0));
					renderPass.bindSampler("Sampler2", RenderSystem.getShaderTexture(2));
					buffers.setIndexBuffer(renderPass, renderType.getRenderPipeline());
					renderPass.setVertexBuffer(0, buffers.vertexBuffer());
					renderPass.drawIndexed(0, buffers.indexCount());
				}

				renderType.clearRenderState();
				modelViewMatrix.popMatrix();
			}
		}
	}
}
