package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import dev.latvian.mods.kmath.color.Color;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public class CanvasFloatUniform extends CanvasUniform {
	private float[] stored;

	public CanvasFloatUniform(String name, UniformType type) {
		super(name, type);
		this.stored = null;
	}

	private void setSize(int size) {
		if (stored == null || stored.length != size) {
			stored = new float[size];
		}
	}

	public CanvasFloatUniform set(float... value) {
		setSize(value.length);
		System.arraycopy(value, 0, stored, 0, value.length);
		return this;
	}

	public CanvasFloatUniform set(float value) {
		setSize(1);
		stored[0] = value;
		return this;
	}

	public CanvasFloatUniform set(float value1, float value2) {
		setSize(2);
		stored[0] = value1;
		stored[1] = value2;
		return this;
	}

	public CanvasFloatUniform set(float value1, float value2, float value3) {
		setSize(3);
		stored[0] = value1;
		stored[1] = value2;
		stored[2] = value3;
		return this;
	}

	public CanvasFloatUniform set(float value1, float value2, float value3, float value4) {
		setSize(4);
		stored[0] = value1;
		stored[1] = value2;
		stored[2] = value3;
		stored[3] = value4;
		return this;
	}

	public CanvasFloatUniform set(Vector3fc vec) {
		set(vec.x(), vec.y(), vec.z());
		return this;
	}

	public CanvasFloatUniform set(Vector4fc vec) {
		set(vec.x(), vec.y(), vec.z(), vec.w());
		return this;
	}

	public CanvasFloatUniform set(Matrix4fc matrix) {
		setSize(16);
		matrix.get(stored);
		return this;
	}

	public CanvasFloatUniform set(Matrix3fc matrix) {
		setSize(9);
		matrix.get(stored);
		return this;
	}

	public CanvasFloatUniform set(Color color) {
		set(color.redf(), color.greenf(), color.bluef(), color.alphaf());
		return this;
	}

	@Override
	public void apply(RenderPass pass) {
		if (stored != null) {
			pass.setUniform(name, stored);
		}
	}
}
