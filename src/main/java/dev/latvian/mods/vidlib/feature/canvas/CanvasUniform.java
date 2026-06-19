package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.UniformValue;
import dev.latvian.mods.klib.util.FloatSupplier;
import org.joml.Matrix4fc;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public abstract class CanvasUniform implements CanvasPassModifier {
	public CanvasUniform bool(String name, BooleanSupplier valueSupplier) {
		return new CanvasBoolUniform(name, valueSupplier);
	}

	public static CanvasUniform int1(String name, IntSupplier valueSupplier) {
		return new CanvasIntUniform(name, UniformValue.Type.INT, u -> u.set(valueSupplier.getAsInt()));
	}

	public static CanvasUniform ivec3(String name, Consumer<CanvasIntUniform> callback) {
		return new CanvasIntUniform(name, UniformValue.Type.IVEC3, callback);
	}

	public static CanvasUniform float1(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformValue.Type.FLOAT, callback);
	}

	public static CanvasUniform float1(String name, FloatSupplier valueSupplier) {
		return new CanvasFloatUniform(name, UniformValue.Type.FLOAT, u -> u.set(valueSupplier.getAsFloat()));
	}

	public static CanvasUniform vec2(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformValue.Type.VEC2, callback);
	}

	public static CanvasUniform vec3(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformValue.Type.VEC3, callback);
	}

	public static CanvasUniform vec4(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformValue.Type.VEC4, callback);
	}

	public static CanvasUniform mat4(String name, Supplier<Matrix4fc> valueSupplier) {
		return new CanvasFloatUniform(name, UniformValue.Type.MATRIX4X4, u -> u.set(valueSupplier.get()));
	}

	public final String name;
	public final UniformValue.Type type;
	private DynamicUniformStorage<DynamicUniformStorage.DynamicUniform> storage;

	public CanvasUniform(String name, UniformValue.Type type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public void build(RenderPipeline.Builder builder) {
		builder.withUniform(name, UniformType.UNIFORM_BUFFER);
	}

	@Override
	public String toString() {
		return name + "[" + type.getSerializedName() + "]";
	}

	protected abstract void addSize(Std140SizeCalculator calculator);

	protected abstract void write(Std140Builder builder);

	protected GpuBufferSlice upload() {
		if (storage == null) {
			var calculator = new Std140SizeCalculator();
			addSize(calculator);
			storage = new DynamicUniformStorage<>("VidLib canvas uniform " + name, calculator.get(), 2);
		}

		return storage.writeUniform(buffer -> write(Std140Builder.intoBuffer(buffer)));
	}
}
