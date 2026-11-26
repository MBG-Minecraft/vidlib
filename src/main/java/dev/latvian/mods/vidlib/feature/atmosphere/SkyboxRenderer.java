package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import dev.latvian.mods.vidlib.feature.client.VLRenderPipelines;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Function;

public class SkyboxRenderer {
	private static final int LONGITUDE_SEGMENTS = 64;
	private static final int LATITUDE_SEGMENTS = 32;
	private static final int SPHERE_QUADS = LONGITUDE_SEGMENTS * LATITUDE_SEGMENTS;

	public static final Map<SkyboxTextureId, Function<Minecraft, AbstractTexture>> CUSTOM_TEXTURES = new Object2ObjectOpenHashMap<>(1);

	public record ChromaKeyData(Vector4fc keyColor, Vector2fc keyCC, Vector2fc range) implements DynamicGpuDataStorage.DynamicGpuData {
		@Override
		public void write(ByteBuffer buffer) {
			Std140Builder.intoBuffer(buffer).putVec4(this.keyColor).putVec2(this.keyCC).putVec2(this.range);
		}
	}

	public static GpuBuffer cubeMap3x2;
	public static GpuBuffer equirectangular;
	public static DynamicGpuDataStorageMapped<ChromaKeyData> chromaKey;

	public static void open() {
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

		int totalVertices = SPHERE_QUADS * 4;

		try (var byteBufferBuilder = ByteBufferBuilder.exactlySized(totalVertices * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
			var buffer = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

			for (int row = 0; row < LATITUDE_SEGMENTS; row++) {
				for (int column = 0; column < LONGITUDE_SEGMENTS; column++) {
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

		chromaKey = new DynamicGpuDataStorageMapped<>("VidLib Skybox Chroma Key UBO", new Std140SizeCalculator().putVec4().putVec2().putVec2().get(), GpuBuffer.USAGE_UNIFORM, 2);
	}

	private static void addSphereVertex(BufferBuilder buffer, int column, int row) {
		float u = (float) column / LONGITUDE_SEGMENTS;
		float v = (float) row / LATITUDE_SEGMENTS;
		double polarAngle = Math.PI * v;
		float ringRadius = row == 0 || row == LATITUDE_SEGMENTS ? 0F : 100F * (float) Math.sin(polarAngle);
		float y = 100F * (float) Math.cos(polarAngle);
		double longitude = Math.TAU * (column % LONGITUDE_SEGMENTS) / LONGITUDE_SEGMENTS;
		float x = -ringRadius * (float) Math.sin(longitude);
		float z = ringRadius * (float) Math.cos(longitude);
		buffer.addVertex(x, y, z).setUv(u, v).setColor(0xFFFFFFFF);
	}

	public static boolean render(RenderPass renderPass, SkyRenderState skyState) {
		var vl = ((VLSkyRenderState) skyState).vl$extra();

		if (vl.skyboxTexture == null) {
			return false;
		}

		int indexCount = vl.skyboxEquirectangular ? (SPHERE_QUADS * 6) : 36;

		var autoIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		var indexBuffer = autoIndices.getBuffer(indexCount);
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.rotate(vl.skyboxRotation);
		var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewStack), vl.skyboxTint);
		renderPass.pushDebugGroup(() -> "VidLib Skybox");
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(vl.skyboxChromaKey.w > 0F ? VLRenderPipelines.SKYBOX_WITH_CHROMA_KEY : VLRenderPipelines.SKYBOX));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", dynamicTransforms);
		renderPass.setUniform("Sampler0", vl.skyboxTexture.getTextureView(), vl.skyboxTexture.getSampler());

		if (vl.skyboxChromaKey.w > 0F) {
			float Y = 0.299F * vl.skyboxChromaKey.x + 0.587F * vl.skyboxChromaKey.y + 0.114F * vl.skyboxChromaKey.z;

			renderPass.setUniform("ChromaKey", chromaKey.writeData(new ChromaKeyData(
				new Vector4f(vl.skyboxChromaKey),
				new Vector2f((vl.skyboxChromaKey.z - Y) * 0.565F, (vl.skyboxChromaKey.x - Y) * 0.713F),
				new Vector2f(vl.skyboxChromaKeyRange.min(), vl.skyboxChromaKeyRange.max())
			)));
		}

		renderPass.setVertexBuffer(0, (vl.skyboxEquirectangular ? equirectangular : cubeMap3x2).slice());
		renderPass.setIndexBuffer(indexBuffer, autoIndices.type());
		renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
		renderPass.popDebugGroup();
		modelViewStack.popMatrix();
		return false;
	}

	public static void close() {
		if (cubeMap3x2 != null) {
			cubeMap3x2.close();
			cubeMap3x2 = null;
		}

		if (equirectangular != null) {
			equirectangular.close();
			equirectangular = null;
		}

		if (chromaKey != null) {
			chromaKey.close();
			chromaKey = null;
		}
	}

	public static void endFrame() {
		if (chromaKey != null) {
			chromaKey.endFrame();
		}
	}
}
