package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SharedIndexBuffer implements IndexBuffer {
	private final RenderSystem.AutoStorageIndexBuffer sequentialBuffer;
	private final int vertices;
	private final int indices;
	private StaticIndexBuffer cached;

	public SharedIndexBuffer(RenderSystem.AutoStorageIndexBuffer sequentialBuffer, int vertices, int indices) {
		this.sequentialBuffer = sequentialBuffer;
		this.vertices = vertices;
		this.indices = indices;
		this.cached = new StaticIndexBuffer(sequentialBuffer.getBuffer(indices), sequentialBuffer.type(), vertices, indices);
	}

	public IndexBuffer cached() {
		if (cached == null || cached.buffer().isClosed()) {
			cached = new StaticIndexBuffer(sequentialBuffer.getBuffer(indices), sequentialBuffer.type(), vertices, indices);
		}

		return cached;
	}

	@Override
	public GpuBuffer buffer() {
		return cached().buffer();
	}

	@Override
	public VertexFormat.IndexType type() {
		return cached().type();
	}

	@Override
	public void close() {
		cached = null;
	}

	@Override
	@Nullable
	public IndexBuffer staticBuffer() {
		return null;
	}

	@Override
	public @NotNull String toString() {
		return "SharedIndexBuffer[vertices=%,d, indices=%,d]".formatted(vertices, indices);
	}
}