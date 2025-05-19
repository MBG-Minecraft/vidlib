package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.core.Vec3i;
import org.joml.Vector3ic;

public class CanvasIntUniform extends CanvasUniform {
	private int[] stored;

	public CanvasIntUniform(String name, UniformType type) {
		super(name, type);
		this.stored = null;
	}

	private void setSize(int size) {
		if (stored == null || stored.length != size) {
			stored = new int[size];
		}
	}

	public CanvasIntUniform set(int... value) {
		setSize(value.length);
		System.arraycopy(value, 0, stored, 0, value.length);
		return this;
	}

	public CanvasIntUniform set(int value) {
		setSize(1);
		stored[0] = value;
		return this;
	}

	public CanvasIntUniform set(int value1, int value2) {
		setSize(2);
		stored[0] = value1;
		stored[1] = value2;
		return this;
	}

	public CanvasIntUniform set(int value1, int value2, int value3) {
		setSize(3);
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
		if (stored != null) {
			pass.setUniform(name, stored);
		}
	}
}
