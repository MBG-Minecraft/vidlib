package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.UniformValue;

import java.util.function.BooleanSupplier;

public class CanvasBoolUniform extends CanvasUniform {
	private final int[] stored;
	private final BooleanSupplier valueSupplier;

	public CanvasBoolUniform(String name, BooleanSupplier valueSupplier) {
		super(name, UniformValue.Type.INT);
		this.stored = new int[1];
		this.valueSupplier = valueSupplier;
	}

	@Override
	public void apply(RenderPass pass) {
		stored[0] = valueSupplier.getAsBoolean() ? 1 : 0;
		pass.setUniform(name, upload());
	}

	@Override
	protected void addSize(Std140SizeCalculator calculator) {
		calculator.putInt();
	}

	@Override
	protected void write(Std140Builder builder) {
		builder.putInt(stored[0]);
	}
}
