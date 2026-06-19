package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.core.Vec3i;
import org.joml.Vector3ic;

import java.util.function.Consumer;

public class CanvasIntUniform extends CanvasUniform {
	private final int[] stored;
	private final Consumer<CanvasIntUniform> callback;

	public CanvasIntUniform(String name, UniformValue.Type type, Consumer<CanvasIntUniform> callback) {
		super(name, type);
		this.stored = new int[size(type)];
		this.callback = callback;
	}

	private static int size(UniformValue.Type type) {
		return switch (type) {
			case INT -> 1;
			case IVEC3 -> 3;
			default -> throw new IllegalArgumentException("Unsupported int uniform type " + type);
		};
	}

	private void testSize(int size) {
		if (stored.length != size) {
			throw new RuntimeException("Size of uniform " + name + " is invalid");
		}
	}

	public CanvasIntUniform set(int... value) {
		testSize(value.length);
		System.arraycopy(value, 0, stored, 0, value.length);
		return this;
	}

	public CanvasIntUniform set(int value) {
		testSize(1);
		stored[0] = value;
		return this;
	}

	public CanvasIntUniform set(int value1, int value2) {
		testSize(2);
		stored[0] = value1;
		stored[1] = value2;
		return this;
	}

	public CanvasIntUniform set(int value1, int value2, int value3) {
		testSize(3);
		stored[0] = value1;
		stored[1] = value2;
		stored[2] = value3;
		return this;
	}

	public CanvasIntUniform set(Vec3i pos) {
		return set(pos.getX(), pos.getY(), pos.getZ());
	}

	public CanvasIntUniform set(Vector3ic pos) {
		return set(pos.x(), pos.y(), pos.z());
	}

	@Override
	public void apply(RenderPass pass) {
		callback.accept(this);
		pass.setUniform(name, upload());
	}

	@Override
	protected void addSize(Std140SizeCalculator calculator) {
		switch (type) {
			case INT -> calculator.putInt();
			case IVEC3 -> calculator.putIVec3();
			default -> throw new IllegalArgumentException("Unsupported int uniform type " + type);
		}
	}

	@Override
	protected void write(Std140Builder builder) {
		switch (type) {
			case INT -> builder.putInt(stored[0]);
			case IVEC3 -> builder.putIVec3(stored[0], stored[1], stored[2]);
			default -> throw new IllegalArgumentException("Unsupported int uniform type " + type);
		}
	}
}
