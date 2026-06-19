package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Vec2f;
import dev.latvian.mods.klib.math.Vec3f;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.util.function.Consumer;

public class CanvasFloatUniform extends CanvasUniform {
	private final float[] stored;
	private final Consumer<CanvasFloatUniform> callback;

	public CanvasFloatUniform(String name, UniformValue.Type type, Consumer<CanvasFloatUniform> callback) {
		super(name, type);
		this.stored = new float[size(type)];
		this.callback = callback;
	}

	private static int size(UniformValue.Type type) {
		return switch (type) {
			case FLOAT -> 1;
			case VEC2 -> 2;
			case VEC3 -> 3;
			case VEC4 -> 4;
			case MATRIX4X4 -> 16;
			default -> throw new IllegalArgumentException("Unsupported float uniform type " + type);
		};
	}

	private void testSize(int size) {
		if (stored.length != size) {
			throw new RuntimeException("Size of uniform " + name + " is invalid");
		}
	}

	public CanvasFloatUniform set(float... value) {
		testSize(value.length);
		System.arraycopy(value, 0, stored, 0, value.length);
		return this;
	}

	public CanvasFloatUniform set(float value) {
		testSize(1);
		stored[0] = value;
		return this;
	}

	public CanvasFloatUniform set(float value1, float value2) {
		testSize(2);
		stored[0] = value1;
		stored[1] = value2;
		return this;
	}

	public CanvasFloatUniform set(Vec2f vec) {
		return set(vec.x(), vec.y());
	}

	public CanvasFloatUniform set(Vec2 vec) {
		return set(vec.x, vec.y);
	}

	public CanvasFloatUniform set(Vector2fc vec) {
		return set(vec.x(), vec.y());
	}

	public CanvasFloatUniform set(float value1, float value2, float value3) {
		testSize(3);
		stored[0] = value1;
		stored[1] = value2;
		stored[2] = value3;
		return this;
	}

	public CanvasFloatUniform set(Vec3f vec) {
		return set(vec.x(), vec.y(), vec.z());
	}

	public CanvasFloatUniform set(Vector3fc vec) {
		set(vec.x(), vec.y(), vec.z());
		return this;
	}

	public CanvasFloatUniform set(float value1, float value2, float value3, float value4) {
		testSize(4);
		stored[0] = value1;
		stored[1] = value2;
		stored[2] = value3;
		stored[3] = value4;
		return this;
	}

	public CanvasFloatUniform set(Vector4fc vec) {
		set(vec.x(), vec.y(), vec.z(), vec.w());
		return this;
	}

	public CanvasFloatUniform set(Color color) {
		set(color.redf(), color.greenf(), color.bluef(), color.alphaf());
		return this;
	}

	public CanvasFloatUniform set(Matrix3fc matrix) {
		testSize(9);
		matrix.get(stored);
		return this;
	}

	public CanvasFloatUniform set(Matrix4fc matrix) {
		testSize(16);
		matrix.get(stored);
		return this;
	}

	@Override
	public void apply(RenderPass pass) {
		callback.accept(this);
		pass.setUniform(name, upload());
	}

	@Override
	protected void addSize(Std140SizeCalculator calculator) {
		switch (type) {
			case FLOAT -> calculator.putFloat();
			case VEC2 -> calculator.putVec2();
			case VEC3 -> calculator.putVec3();
			case VEC4 -> calculator.putVec4();
			case MATRIX4X4 -> calculator.putMat4f();
			default -> throw new IllegalArgumentException("Unsupported float uniform type " + type);
		}
	}

	@Override
	protected void write(Std140Builder builder) {
		switch (type) {
			case FLOAT -> builder.putFloat(stored[0]);
			case VEC2 -> builder.putVec2(stored[0], stored[1]);
			case VEC3 -> builder.putVec3(stored[0], stored[1], stored[2]);
			case VEC4 -> builder.putVec4(stored[0], stored[1], stored[2], stored[3]);
			case MATRIX4X4 -> builder.putMat4f(new org.joml.Matrix4f().set(stored));
			default -> throw new IllegalArgumentException("Unsupported float uniform type " + type);
		}
	}
}
