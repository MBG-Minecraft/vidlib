package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import dev.latvian.mods.vidlib.util.FloatSupplier;
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
		return new CanvasIntUniform(name, UniformType.INT, u -> u.set(valueSupplier.getAsInt()));
	}

	public static CanvasUniform ivec3(String name, Consumer<CanvasIntUniform> callback) {
		return new CanvasIntUniform(name, UniformType.IVEC3, callback);
	}

	public static CanvasUniform float1(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformType.FLOAT, callback);
	}

	public static CanvasUniform float1(String name, FloatSupplier valueSupplier) {
		return new CanvasFloatUniform(name, UniformType.FLOAT, u -> u.set(valueSupplier.getAsFloat()));
	}

	public static CanvasUniform vec2(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformType.VEC2, callback);
	}

	public static CanvasUniform vec3(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformType.VEC3, callback);
	}

	public static CanvasUniform vec4(String name, Consumer<CanvasFloatUniform> callback) {
		return new CanvasFloatUniform(name, UniformType.VEC4, callback);
	}

	public static CanvasUniform mat4(String name, Supplier<Matrix4fc> valueSupplier) {
		return new CanvasFloatUniform(name, UniformType.MATRIX4X4, u -> u.set(valueSupplier.get()));
	}

	public final String name;
	public final UniformType type;

	public CanvasUniform(String name, UniformType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public void build(RenderPipeline.Builder builder) {
		builder.withUniform(name, type);
	}

	@Override
	public String toString() {
		return name + "[" + type.getSerializedName() + "]";
	}
}
