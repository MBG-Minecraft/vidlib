package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;

import java.util.function.BooleanSupplier;

public class CanvasBoolUniform extends CanvasUniform {
	private final int[] stored;
	private final BooleanSupplier valueSupplier;

	public CanvasBoolUniform(String name, BooleanSupplier valueSupplier) {
		super(name, UniformType.INT);
		this.stored = new int[1];
		this.valueSupplier = valueSupplier;
	}

	@Override
	public void apply(RenderPass pass) {
		stored[0] = valueSupplier.getAsBoolean() ? 1 : 0;
		pass.setUniform(name, stored);
	}
}
