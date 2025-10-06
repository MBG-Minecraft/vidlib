package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Vec2f;
import dev.latvian.mods.klib.math.Vec3f;
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

	public CanvasFloatUniform(String name, UniformType type, Consumer<CanvasFloatUniform> callback) {
		super(name, type);
		this.stored = new float[type.count()];
		this.callback = callback;
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
		pass.setUniform(name, stored);
	}
}
