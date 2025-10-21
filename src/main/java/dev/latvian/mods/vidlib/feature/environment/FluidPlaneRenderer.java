package dev.latvian.mods.vidlib.feature.environment;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.gl.StaticBuffers;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.TerrainRenderTypes;
import dev.latvian.mods.vidlib.feature.visual.ResolvedCubeTextures;
import dev.latvian.mods.vidlib.feature.visual.ResolvedTexturedCube;
import dev.latvian.mods.vidlib.feature.visual.TexturedCubeRenderer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class FluidPlaneRenderer {
	public static final Map<ResourceLocation, StaticBuffers> BUFFERS = new HashMap<>();

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void texturesReloaded() {
		for (var buffer : BUFFERS.values()) {
			buffer.close();
		}

		BUFFERS.clear();
	}

	public static void render(FrameInfo frame, FluidPlane fluidPlane) {
		var tex = ResolvedCubeTextures.resolve(fluidPlane.fluid().textures().cubeTextures());

		if (tex.anyIn(frame.layer())) {
			var tex1 = tex.filter(frame.layer(), true);

			if (!tex1.isEmpty()) {
				int r = 256;
				double s = 8192D;
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

				TexturedCubeRenderer.render(frame, light, new ResolvedTexturedCube(new AABB(x - s, y, z - r, x - r, y, z + r + 1D), tex), Color.WHITE);
				TexturedCubeRenderer.render(frame, light, new ResolvedTexturedCube(new AABB(x + r + 1D, y, z - r, x + s + 1D, y, z + r + 1D), tex), Color.WHITE);
				TexturedCubeRenderer.render(frame, light, new ResolvedTexturedCube(new AABB(x - s, y, z - s, x + s + 1D, y, z - r), tex), Color.WHITE);
				TexturedCubeRenderer.render(frame, light, new ResolvedTexturedCube(new AABB(x - s, y, z + r + 1D, x + s + 1D, y, z + s + 1D), tex), Color.WHITE);

				var buffers = BUFFERS.get(texture);
				var renderType = TerrainRenderTypes.get(frame.layer(), false).apply(TextureAtlas.LOCATION_BLOCKS);

				if (buffers == null || !buffers.isEmpty() && buffers.vertexBuffer().isClosed()) {
					var format = DefaultVertexFormat.BLOCK;
					buffers = StaticBuffers.empty(format);

					var mc = Minecraft.getInstance();
					var sprite = mc.getBlockAtlas().getSprite(texture);
					var uv = new UV(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());

					try (var memory = new ByteBufferBuilder(format.getVertexSize() * 4 * r * r)) {
						var buffer = new BufferBuilder(memory, VertexFormat.Mode.QUADS, format);

						for (int pz = 0; pz < r * 2 + 1; pz++) {
							for (int px = 0; px < r * 2 + 1; px++) {
								float minX = px;
								float minZ = pz;
								float maxX = px + 1F;
								float maxZ = pz + 1F;
								buffer.addVertex(minX, 0F, minZ).setColor(colR, colG, colB, colA).setUv(uv.u0(), uv.v0()).setUv2(lu, lv).setNormal(0F, 1F, 0F);
								buffer.addVertex(minX, 0F, maxZ).setColor(colR, colG, colB, colA).setUv(uv.u0(), uv.v1()).setUv2(lu, lv).setNormal(0F, 1F, 0F);
								buffer.addVertex(maxX, 0F, maxZ).setColor(colR, colG, colB, colA).setUv(uv.u1(), uv.v1()).setUv2(lu, lv).setNormal(0F, 1F, 0F);
								buffer.addVertex(maxX, 0F, minZ).setColor(colR, colG, colB, colA).setUv(uv.u1(), uv.v0()).setUv2(lu, lv).setNormal(0F, 1F, 0F);
							}
						}

						try (var meshData = buffer.build()) {
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

				var modelViewMatrix = RenderSystem.getModelViewStack();
				modelViewMatrix.pushMatrix();
				modelViewMatrix.translate(
					frame.x(x - r),
					frame.y(y),
					frame.z(z - r)
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
