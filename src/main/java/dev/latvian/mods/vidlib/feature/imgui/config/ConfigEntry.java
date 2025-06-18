package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

import java.util.Objects;

public abstract class ConfigEntry<T> {
	public static ConfigEntry<Boolean> bool(String label, DataKey<Boolean> key) {
		return new BoolConfigEntry(label, key);
	}

	public static ConfigEntry<Integer> intInput(String label, DataKey<Integer> key, int min, int max) {
		return new IntConfigEntry(label, key, min, max, false);
	}

	public static ConfigEntry<Integer> intInput(String label, DataKey<Integer> key, int max) {
		return intInput(label, key, 0, max);
	}

	public static ConfigEntry<Integer> intSlider(String label, DataKey<Integer> key, int min, int max) {
		return new IntConfigEntry(label, key, min, max, true);
	}

	public static ConfigEntry<Integer> intSlider(String label, DataKey<Integer> key, int max) {
		return intSlider(label, key, 0, max);
	}

	public static ConfigEntry<Float> floatInput(String label, DataKey<Float> key, float min, float max) {
		return new FloatConfigEntry(label, key, min, max, false);
	}

	public static ConfigEntry<Float> floatInput(String label, DataKey<Float> key, float max) {
		return floatInput(label, key, 0F, max);
	}

	public static ConfigEntry<Float> floatSlider(String label, DataKey<Float> key, float min, float max) {
		return new FloatConfigEntry(label, key, min, max, true);
	}

	public static ConfigEntry<Float> floatSlider(String label, DataKey<Float> key, float max) {
		return floatSlider(label, key, 0F, max);
	}

	public static ConfigEntry<Double> doubleInput(String label, DataKey<Double> key, double min, double max) {
		return new DoubleInputConfigEntry(label, key, min, max);
	}

	public static ConfigEntry<Double> doubleInput(String label, DataKey<Double> key, double max) {
		return doubleInput(label, key, 0D, max);
	}

	public static ConfigEntry<Double> doubleSlider(String label, DataKey<Double> key, double min, double max) {
		return new DoubleSliderConfigEntry(label, key, min, max);
	}

	public static ConfigEntry<Double> doubleSlider(String label, DataKey<Double> key, double max) {
		return doubleSlider(label, key, 0D, max);
	}

	public static ConfigEntry<Gradient> gradient(String label, DataKey<Gradient> key) {
		return new GradientConfigEntry(label, key);
	}

	public final String label;
	public final DataKey<T> key;
	public final String id;

	public ConfigEntry(String label, DataKey<T> key) {
		this.label = label;
		this.key = key;
		this.id = "###" + key.id().replace('/', '-');
	}

	public void init(DataMap dataMap) {
		set(dataMap.get(key));
	}

	public abstract T get();

	public abstract void set(T value);

	public boolean imguiSameLine() {
		return false;
	}

	public boolean imguiLabel() {
		var isSame = equals(get(), key.defaultValue());

		if (isSame) {
			ImGui.text(label);
		} else {
			ImGui.pushStyleColor(ImGuiCol.Text, 0xFF00D8FF);
			ImGui.text(label);
			ImGui.popStyleColor();
		}

		ImGui.sameLine();

		if (isSame) {
			ImGui.beginDisabled();
		}

		boolean reset = ImGui.smallButton("Reset" + id + "-reset");

		if (isSame) {
			ImGui.endDisabled();
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip(json(key.defaultValue()));
		}

		if (reset) {
			set(key.defaultValue());
			return true;
		}

		return false;
	}

	public abstract boolean imguiValue();

	public String json(T value) {
		return value.toString();
	}

	public boolean equals(T a, T b) {
		return Objects.equals(a, b);
	}
}
