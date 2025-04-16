package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;

public record IndexBuffer(GpuBuffer buffer, VertexFormat.IndexType type, int vertices, int count, boolean shouldClose) {
	public static IndexBuffer of(VertexFormat.Mode mode, int vertices) {
		var sequentialBuffer = RenderSystem.getSequentialBuffer(mode);
		int count = mode.indexCount(vertices);
		var buffer = sequentialBuffer.getBuffer(count);
		var type = sequentialBuffer.type();
		return new IndexBuffer(buffer, type, vertices, count, false);
	}

	public static IndexBuffer of(MeshData meshData) {
		int vertices = meshData.drawState().vertexCount();
		var indexBuffer = meshData.indexBuffer();

		if (indexBuffer == null) {
			return of(meshData.drawState().mode(), vertices);
		} else {
			int count = meshData.drawState().indexCount();
			var buffer = RenderSystem.getDevice().createBuffer(null, BufferType.INDICES, BufferUsage.STATIC_WRITE, indexBuffer);
			var type = meshData.drawState().indexType();
			return new IndexBuffer(buffer, type, vertices, count, true);
		}
	}

	public void close() {
		if (shouldClose) {
			buffer.close();
		}
	}
}
