package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import imgui.ImGui;

public class DoubleSliderConfigEntry extends ConfigEntry<Double> {
	public final float[] data;
	public final double min;
	public final double max;

	public DoubleSliderConfigEntry(String label, DataKey<Double> key, double min, double max) {
		super(label, key);
		this.data = new float[1];
		this.min = min;
		this.max = max;
	}

	@Override
	public Double get() {
		return (double) data[0];
	}

	@Override
	public void set(Double value) {
		data[0] = value.floatValue();
	}

	@Override
	public boolean imguiValue() {
		ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - ImGui.getStyle().getItemSpacingX());
		return ImGui.sliderFloat(id, data, (float) min, (float) max);
	}

	@Override
	public boolean equals(Double a, Double b) {
		return Math.abs(a - b) < 0.000001D;
	}
}
