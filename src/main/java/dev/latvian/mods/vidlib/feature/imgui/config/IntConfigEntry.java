package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImInt;

public class IntConfigEntry extends ConfigEntry<Integer> {
	public final ImInt data;
	public final int min;
	public final int max;
	public final boolean slider;

	public IntConfigEntry(String label, DataKey<Integer> key, int min, int max, boolean slider) {
		super(label, key);
		this.data = new ImInt();
		this.min = min;
		this.max = max;
		this.slider = slider;
	}

	@Override
	public Integer get() {
		return data.get();
	}

	@Override
	public void set(Integer value) {
		data.set(value);
	}

	@Override
	public ImUpdate imguiValue(ImGraphics graphics) {
		if (slider) {
			ImGui.sliderInt(id, data.getData(), min, max);
		} else {
			ImGui.inputInt(id, data);
		}

		return ImUpdate.itemEdit();
	}
}
