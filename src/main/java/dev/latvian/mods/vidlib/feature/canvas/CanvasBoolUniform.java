package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;

public class CanvasBoolUniform extends CanvasUniform {
	private final int[] stored;

	public CanvasBoolUniform(String name) {
		super(name, UniformType.INT);
		this.stored = new int[1];
	}

	public CanvasBoolUniform set(boolean value) {
		stored[0] = value ? 1 : 0;
		return this;
	}

	@Override
	public void apply(RenderPass pass) {
		pass.setUniform(name, stored);
	}
}
