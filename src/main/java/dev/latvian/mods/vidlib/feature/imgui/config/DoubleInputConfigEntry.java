package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImDouble;

public class DoubleInputConfigEntry extends ConfigEntry<Double> {
	public final ImDouble data;
	public final double min;
	public final double max;

	public DoubleInputConfigEntry(String label, DataKey<Double> key, double min, double max) {
		super(label, key);
		this.data = new ImDouble();
		this.min = min;
		this.max = max;
	}

	@Override
	public Double get() {
		return data.get();
	}

	@Override
	public void set(Double value) {
		data.set(value);
	}

	@Override
	public ImUpdate imguiValue(ImGraphics graphics) {
		ImGui.inputDouble(id, data);
		return ImUpdate.itemEdit();
	}

	@Override
	public boolean equals(Double a, Double b) {
		return Math.abs(a - b) < 0.000001D;
	}
}
