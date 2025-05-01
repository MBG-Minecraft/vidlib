package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public record StaticBuffers(VertexFormat format, int vertexCount, int indexCount, GpuBuffer vertexBuffer, @Nullable IndexBuffer indexBuffer) implements AutoCloseable {
	@Override
	public void close() {
		if (vertexBuffer != null) {
			vertexBuffer.close();
		}

		if (indexBuffer != null) {
			indexBuffer.close();
		}
	}

	public boolean isEmpty() {
		return vertexCount == 0 || indexCount == 0;
	}

	public static StaticBuffers empty(VertexFormat format) {
		return new StaticBuffers(format, 0, 0, null, null);
	}

	public static StaticBuffers of(MeshData meshData, @Nullable Supplier<String> name, @Nullable Optional<IndexBuffer> indexBuffer) {
		var format = meshData.drawState().format();
		var vertexBuf = RenderSystem.getDevice().createBuffer(name, BufferType.VERTICES, BufferUsage.STATIC_WRITE, meshData.vertexBuffer());
		var indexBuf = indexBuffer == null ? IndexBuffer.of(meshData) : indexBuffer.orElse(null);
		return new StaticBuffers(format, meshData.drawState().vertexCount(), meshData.drawState().indexCount(), vertexBuf, indexBuf == null ? null : indexBuf.staticBuffer());
	}

	public static StaticBuffers of(MeshData meshData, @Nullable Supplier<String> name) {
		return of(meshData, name, null);
	}

	public void setIndexBuffer(RenderPass renderPass, RenderPipeline pipeline) {
		if (indexBuffer == null) {
			var buf = IndexBuffer.of(pipeline.getVertexFormatMode(), vertexCount);
			renderPass.setIndexBuffer(buf.buffer(), buf.type());
		} else {
			renderPass.setIndexBuffer(indexBuffer.buffer(), indexBuffer.type());
		}
	}
}
