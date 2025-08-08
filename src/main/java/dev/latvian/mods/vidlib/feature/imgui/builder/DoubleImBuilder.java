package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

public class DoubleImBuilder implements ImBuilder<Double> {
	public static ImBuilderType<Double> type(double min, double max) {
		return () -> new DoubleImBuilder(min, max);
	}

	public static final ImBuilderType<Double> TYPE = type(0D, 1D);

	public final float[] value;
	public final double min, max;

	public DoubleImBuilder(double min, double max) {
		this.value = new float[]{0F};
		this.min = min;
		this.max = max;
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
