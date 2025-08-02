package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

public class DoubleImBuilder implements ImBuilder<Double> {
	public static final ImBuilderSupplier<Double> SUPPLIER = DoubleImBuilder::new;

	public final float[] value;
	public final double min, max;

	public DoubleImBuilder(double min, double max) {
		this.value = new float[]{0F};
		this.min = min;
		this.max = max;
	}

	public DoubleImBuilder(double max) {
		this(0D, max);
	}

	public DoubleImBuilder() {
		this(0D, 1D);
	}

	@Override
	public void set(Double v) {
		value[0] = v.floatValue();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.sliderFloat("###double", value, (float) min, (float) max);
		return ImUpdate.itemEdit();
	}

	@Override
	public Double build() {
		return (double) value[0];
	}

	@Override
	public boolean equals(Double a, Double b) {
		return Math.abs(a - b) < 0.0001D;
	}
}
