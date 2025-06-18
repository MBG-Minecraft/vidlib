package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import imgui.ImGui;
import imgui.type.ImBoolean;

public class BoolConfigEntry extends ConfigEntry<Boolean> {
	public final ImBoolean data;

	public BoolConfigEntry(String label, DataKey<Boolean> key) {
		super(label, key);
		this.data = new ImBoolean();
	}

	@Override
	public Boolean get() {
		return data.get();
	}

	@Override
	public void set(Boolean value) {
		data.set(value);
	}

	@Override
	public boolean imguiSameLine() {
		return true;
	}

	@Override
	public boolean imguiValue() {
		return ImGui.checkbox(id, data);
	}
}
