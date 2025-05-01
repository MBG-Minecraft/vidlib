package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;

public interface IndexBuffer extends AutoCloseable {
	static IndexBuffer of(VertexFormat.Mode mode, int vertices) {
		var sequentialBuffer = RenderSystem.getSequentialBuffer(mode);
		int indices = mode.indexCount(vertices);
		return new SharedIndexBuffer(sequentialBuffer, vertices, indices);
	}

	static IndexBuffer of(MeshData meshData) {
		var mode = meshData.drawState().mode();
		int vertices = meshData.drawState().vertexCount();
		var indexBuffer = meshData.indexBuffer();

		if (indexBuffer == null) {
			return of(mode, vertices);
		} else {
			int count = meshData.drawState().indexCount();
			var buffer = RenderSystem.getDevice().createBuffer(null, BufferType.INDICES, BufferUsage.STATIC_WRITE, indexBuffer);
			var type = meshData.drawState().indexType();
			return new StaticIndexBuffer(buffer, type, vertices, count);
		}
	}

	GpuBuffer buffer();

	VertexFormat.IndexType type();

	@Override
	default void close() {
	}

	@Nullable
	default IndexBuffer staticBuffer() {
		return this;
	}
}
