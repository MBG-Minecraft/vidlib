package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import org.lwjgl.system.MemoryStack;

import java.util.List;

public class CustomPostPassUniforms {
	public final String name;
	public final List<UniformSupplier> uniforms;
	public final int size;
	private boolean update;
	private GpuBuffer uniformBuffer;

	public CustomPostPassUniforms(String name, List<UniformSupplier> uniformSuppliers) {
		this.name = name;
		this.uniforms = List.copyOf(uniformSuppliers);

		var calculator = new Std140SizeCalculator();

		for (var uniform : uniforms) {
			uniform.value().get().addSize(calculator);
		}

		this.size = calculator.get();
		this.update = true;
	}

	public void update() {
		this.update = true;
	}

	@Override
	public String toString() {
		return name;
	}

	public GpuBuffer getBuffer() {
		if (update) {
			update = false;

			if (uniformBuffer != null) {
				uniformBuffer.close();
				uniformBuffer = null;
			}

			try (var stack = MemoryStack.stackPush()) {
				var builder = Std140Builder.onStack(stack, size);

				for (var uniform : uniforms) {
					uniform.value().get().writeTo(builder);
				}

				uniformBuffer = RenderSystem.getDevice().createBuffer(this::toString, GpuBuffer.USAGE_UNIFORM, builder.get());
			}
		}

		return uniformBuffer;
	}
}
