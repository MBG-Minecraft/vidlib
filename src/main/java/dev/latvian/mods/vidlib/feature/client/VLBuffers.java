package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;

public class VLBuffers {
	public static final int EQUIRECTANGULAR_LNG_SEGMENTS = 64;
	public static final int EQUIRECTANGULAR_LAT_SEGMENTS = 32;
	public static final int EQUIRECTANGULAR_QUAD_COUNT = EQUIRECTANGULAR_LNG_SEGMENTS * EQUIRECTANGULAR_LAT_SEGMENTS;

	public static GpuBuffer cubeMap3x2;
	public static GpuBuffer equirectangular;
	public static DynamicGpuDataStorageMapped<ChromaKeyData> chromaKey;

	public static void build() {
		try (var byteBufferBuilder = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
			var buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			float ps = 100F;
			float ns = -ps;

			// Down / Bottom
			{
				float u0 = 0F;
				float v0 = 0.5F;
				float u1 = 1F / 3F;
				float v1 = 1F;
				buffer.addVertex(ns, ns, ns).setUv(u0, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ns, ps).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ps).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ns).setUv(u1, v0).setColor(0xFFFFFFFF);
			}

			// Up / Top
			{
				float u0 = 2F / 3F;
				float v0 = 0F;
				float u1 = 1F;
				float v1 = 0.5F;
				buffer.addVertex(ns, ps, ns).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ns).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ps).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ps).setUv(u0, v0).setColor(0xFFFFFFFF);
			}

			// North / Front
			{
				float u0 = 1F / 3F;
				float v0 = 0.5F;
				float u1 = 2F / 3F;
				float v1 = 1F;
				buffer.addVertex(ns, ns, ns).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ns).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ns).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ns).setUv(u0, v0).setColor(0xFFFFFFFF);
			}

			// South / Back
			{
				float u0 = 2F / 3F;
				float v0 = 0.5F;
				float u1 = 1F;
				float v1 = 1F;
				buffer.addVertex(ns, ns, ps).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ps).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ps).setUv(u0, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ps).setUv(u0, v1).setColor(0xFFFFFFFF);
			}

			// West / Left
			{
				float u0 = 1F / 3F;
				float v0 = 0F;
				float u1 = 2F / 3F;
				float v1 = 0.5F;
				buffer.addVertex(ns, ns, ns).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ns).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ps, ps).setUv(u0, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ns, ns, ps).setUv(u0, v1).setColor(0xFFFFFFFF);
			}

			// East / Right
			{
				float u0 = 0F;
				float v0 = 0F;
				float u1 = 1F / 3F;
				float v1 = 0.5F;
				buffer.addVertex(ps, ns, ns).setUv(u0, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ns, ps).setUv(u1, v1).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ps).setUv(u1, v0).setColor(0xFFFFFFFF);
				buffer.addVertex(ps, ps, ns).setUv(u0, v0).setColor(0xFFFFFFFF);
			}

			try (var meshData = buffer.buildOrThrow()) {
				cubeMap3x2 = RenderSystem.getDevice().createBuffer(() -> "VidLib Skybox cube map 3x2 vertex buffer", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());
			}
		}

		int totalVertices = EQUIRECTANGULAR_QUAD_COUNT * 4;

		try (var byteBufferBuilder = ByteBufferBuilder.exactlySized(totalVertices * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
			var buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

			for (int row = 0; row < EQUIRECTANGULAR_LAT_SEGMENTS; row++) {
				for (int column = 0; column < EQUIRECTANGULAR_LNG_SEGMENTS; column++) {
					addSphereVertex(buffer, column, row + 1);
					addSphereVertex(buffer, column + 1, row + 1);
					addSphereVertex(buffer, column + 1, row);
					addSphereVertex(buffer, column, row);
				}
			}

			try (var meshData = buffer.buildOrThrow()) {
				equirectangular = RenderSystem.getDevice().createBuffer(() -> "VidLib Skybox equirectangular vertex buffer", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());
			}
		}

		chromaKey = new DynamicGpuDataStorageMapped<>("VidLib Skybox Chroma Key UBO", new Std140SizeCalculator()
			.putVec4()
			.putVec2()
			.putVec2()
			.get(), GpuBuffer.USAGE_UNIFORM, 2
		);
	}

	private static void addSphereVertex(BufferBuilder buffer, int column, int row) {
		float u = (float) column / EQUIRECTANGULAR_LNG_SEGMENTS;
		float v = (float) row / EQUIRECTANGULAR_LAT_SEGMENTS;
		double polarAngle = Math.PI * v;
		float ringRadius = row == 0 || row == EQUIRECTANGULAR_LAT_SEGMENTS ? 0F : 100F * (float) Math.sin(polarAngle);
		float y = 100F * (float) Math.cos(polarAngle);
		double longitude = Math.TAU * (column % EQUIRECTANGULAR_LNG_SEGMENTS) / EQUIRECTANGULAR_LNG_SEGMENTS;
		float x = -ringRadius * (float) Math.sin(longitude);
		float z = ringRadius * (float) Math.cos(longitude);
		buffer.addVertex(x, y, z).setUv(u, v).setColor(0xFFFFFFFF);
	}

	public static void close() {
		cubeMap3x2.close();
		equirectangular.close();
		chromaKey.close();
	}

	public static void endFrame() {
		if (chromaKey != null) {
			chromaKey.endFrame();
		}
	}
}
