package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.FloatSupplier;
import net.minecraft.client.renderer.UniformValue;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public record UniformSupplier(String name, Supplier<? extends UniformValue> value) {
	public static UniformSupplier of(String name, FloatSupplier value) {
		var p = new float[]{value.getAsFloat()};
		var v = new UniformValue.FloatUniform[]{new UniformValue.FloatUniform(p[0])};

		return new UniformSupplier(name, () -> {
			float pv = value.getAsFloat();

			if (pv != p[0]) {
				p[0] = pv;
				v[0] = new UniformValue.FloatUniform(pv);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, Vector3i value) {
		var v = new UniformValue.IVec3Uniform(value);
		return new UniformSupplier(name, () -> v);
	}

	public static UniformSupplier of(String name, IntSupplier value) {
		var p = new int[]{value.getAsInt()};
		var v = new UniformValue.IntUniform[]{new UniformValue.IntUniform(p[0])};

		return new UniformSupplier(name, () -> {
			int pv = value.getAsInt();

			if (pv != p[0]) {
				p[0] = pv;
				v[0] = new UniformValue.IntUniform(pv);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, Vector2f value) {
		var v = new UniformValue.Vec2Uniform(value);
		return new UniformSupplier(name, () -> v);
	}

	public static UniformSupplier of(String name, Vector3f value) {
		var v = new UniformValue.Vec3Uniform(value);
		return new UniformSupplier(name, () -> v);
	}

	public static UniformSupplier of(String name, Vector4f value) {
		var v = new UniformValue.Vec4Uniform(value);
		return new UniformSupplier(name, () -> v);
	}

	public static UniformSupplier of(String name, Matrix4f value) {
		var v = new UniformValue.Matrix4x4Uniform(value);
		return new UniformSupplier(name, () -> v);
	}
}
