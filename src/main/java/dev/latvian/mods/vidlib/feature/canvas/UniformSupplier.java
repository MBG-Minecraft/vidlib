package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.util.FloatSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.UniformValue;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public record UniformSupplier(String name, Supplier<? extends UniformValue> value) {
	public static UniformSupplier of(String name, FloatSupplier value) {
		var p = new float[]{value.getAsFloat()};
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			var nv = value.getAsFloat();

			if (v[0] == null || p[0] != nv) {
				p[0] = nv;
				v[0] = new UniformValue.FloatUniform(nv);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, Vector3i value) {
		var p = new Vector3i(value);
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			if (v[0] == null || !p.equals(value)) {
				p.set(value);
				v[0] = new UniformValue.IVec3Uniform(p);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, IntSupplier value) {
		var p = new float[]{value.getAsInt()};
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			var nv = value.getAsInt();

			if (v[0] == null || p[0] != nv) {
				p[0] = nv;
				v[0] = new UniformValue.IntUniform(nv);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, Vector2f value) {
		var p = new Vector2f(value);
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			if (v[0] == null || !p.equals(value)) {
				p.set(value);
				v[0] = new UniformValue.Vec2Uniform(p);
			}

			return v[0];
		});
	}

	public static UniformSupplier ofVec2(String name, Consumer<Vector2f> callback) {
		var value = new Vector2f(0F, 0F);
		var p = new Vector2f(0F, 0F);
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			callback.accept(value);

			if (v[0] == null || !p.equals(value)) {
				p.set(value);
				v[0] = new UniformValue.Vec2Uniform(p);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, Vector3f value) {
		var p = new Vector3f(value);
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			if (v[0] == null || !p.equals(value)) {
				p.set(value);
				v[0] = new UniformValue.Vec3Uniform(p);
			}

			return v[0];
		});
	}

	public static UniformSupplier ofVec3(String name, Consumer<Vector3d> callback) {
		var value = new Vector3d(0D, 0D, 0D);
		var p = new Vector3f(0F, 0F, 0F);
		var op = new Vector3f(0F, 0F, 0F);
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			callback.accept(value);
			var cam = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.pos;
			op.set(value.x - cam.x, value.y - cam.y, value.z - cam.z);

			if (v[0] == null || !p.equals(op)) {
				p.set(op);
				v[0] = new UniformValue.Vec3Uniform(p);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, Vector4f value) {
		var p = new Vector4f(value);
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			if (v[0] == null || !p.equals(value)) {
				p.set(value);
				v[0] = new UniformValue.Vec4Uniform(p);
			}

			return v[0];
		});
	}

	public static UniformSupplier of(String name, Matrix4f value) {
		var p = new Matrix4f(value);
		var v = new UniformValue[]{null};

		return new UniformSupplier(name, () -> {
			if (v[0] == null || !p.equals(value)) {
				p.set(value);
				v[0] = new UniformValue.Matrix4x4Uniform(p);
			}

			return v[0];
		});
	}
}
