package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
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
	public ImUpdate imguiValue(ImGraphics graphics) {
		if (slider) {
			ImGui.sliderFloat(id, data.getData(), min, max);
		} else {
			ImGui.inputFloat(id, data);
		}

		return ImUpdate.itemEdit();
	}

	@Override
	public boolean equals(Float a, Float b) {
		return Math.abs(a - b) < 0.000001F;
	}
}
