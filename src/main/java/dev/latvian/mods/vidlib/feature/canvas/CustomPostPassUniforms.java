package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.minecraft.client.renderer.UniformValue;
import org.lwjgl.system.MemoryStack;

import java.util.List;

public class CustomPostPassUniforms {
	public final String name;
	public final List<UniformSupplier> uniforms;
	private final UniformValue[] currentValues;
	public final int size;
	private GpuBuffer uniformBuffer;

	public CustomPostPassUniforms(String name, List<UniformSupplier> uniformSuppliers) {
		this.name = name;
		this.uniforms = List.copyOf(uniformSuppliers);
		this.currentValues = new UniformValue[uniforms.size()];

		var calculator = new Std140SizeCalculator();

		for (var uniform : uniforms) {
			uniform.value().get().addSize(calculator);
		}

		this.size = calculator.get();

		for (int i = 0; i < currentValues.length; i++) {
			currentValues[i] = uniforms.get(i).value().get();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public GpuBuffer getBuffer() {
		boolean update = false;

		for (int i = 0; i < currentValues.length; i++) {
			var v = uniforms.get(i).value().get();

			if (currentValues[i] != v) {
				currentValues[i] = v;
				update = true;
			}
		}

		if (update) {
			if (uniformBuffer != null) {
				uniformBuffer.close();
				uniformBuffer = null;
			}

			try (var stack = MemoryStack.stackPush()) {
				var builder = Std140Builder.onStack(stack, size);

				for (var value : currentValues) {
					value.writeTo(builder);
				}

				uniformBuffer = RenderSystem.getDevice().createBuffer(this::toString, GpuBuffer.USAGE_UNIFORM, builder.get());
			}
		}

		return uniformBuffer;
	}
}
