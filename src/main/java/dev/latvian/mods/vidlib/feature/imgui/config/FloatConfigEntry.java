package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import imgui.ImGui;
import imgui.type.ImFloat;

public class FloatConfigEntry extends ConfigEntry<Float> {
	public final ImFloat data;
	public final float min;
	public final float max;
	public final boolean slider;

	public FloatConfigEntry(String label, DataKey<Float> key, float min, float max, boolean slider) {
		super(label, key);
		this.data = new ImFloat();
		this.min = min;
		this.max = max;
		this.slider = slider;
	}

	@Override
	public Float get() {
		return data.get();
	}

	@Override
	public void set(Float value) {
		data.set(value);
	}

	@Override
	public boolean imguiValue() {
		ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - ImGui.getStyle().getItemSpacingX());

		if (slider) {
			return ImGui.sliderFloat(id, data.getData(), min, max);
		}

		return ImGui.inputFloat(id, data);
	}

	@Override
	public boolean equals(Float a, Float b) {
		return Math.abs(a - b) < 0.000001F;
	}
}
