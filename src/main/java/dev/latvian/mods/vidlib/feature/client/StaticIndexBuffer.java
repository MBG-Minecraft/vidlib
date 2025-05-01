package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

record StaticIndexBuffer(GpuBuffer buffer, VertexFormat.IndexType type, int vertices, int indices) implements IndexBuffer {
	@Override
	public void close() {
		buffer.close();
	}

	@Override
	public @NotNull String toString() {
		return "StaticIndexBuffer[size=%,d bytes, vertices=%,d, indices=%,d, type=%s]".formatted(buffer.size, vertices, indices, type.toString().toLowerCase(Locale.ROOT));
	}
}
