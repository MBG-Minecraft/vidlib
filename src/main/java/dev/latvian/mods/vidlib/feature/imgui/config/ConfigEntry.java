package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.vidlib.core.VLMinecraftClient;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

import java.util.Objects;

public abstract class ConfigEntry<T> {
	public enum Update {
		NONE,
		PARTIAL,
		FULL;

		public static final Update[] VALUES = values();

		public static Update full(boolean value) {
			return value ? FULL : NONE;
		}

		public static Update itemEdit() {
			if (ImGui.isItemDeactivatedAfterEdit()) {
				return Update.FULL;
			} else if (ImGui.isItemEdited()) {
				return Update.PARTIAL;
			} else {
				return Update.NONE;
			}
		}

		public Update or(Update other) {
			return VALUES[Math.max(ordinal(), other.ordinal())];
		}
	}

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

	public static ConfigEntry<Color> color(String label, DataKey<Color> key) {
		return new ColorConfigEntry(label, key);
	}

	public static ConfigEntry<Gradient> gradient(String label, DataKey<Gradient> key) {
		return new GradientConfigEntry(label, key);
	}

	public final String label;
	public final DataKey<T> key;
	public final String id;
	public Object extraData;

	public ConfigEntry(String label, DataKey<T> key) {
		this.label = label;
		this.key = key;
		this.id = "###" + key.id().replace('/', '-');
	}

	public ConfigEntry<T> withExtraData(Object extraData) {
		this.extraData = extraData;
		return this;
	}

	public void init(DataMap dataMap) {
		set(dataMap.get(key));
	}

	public abstract T get();

	public abstract void set(T value);

	public boolean imguiSameLine() {
		return false;
	}

	public Update imguiLabel() {
		var isDefault = isDefault();

		if (isDefault) {
			ImGui.text(label);
		} else {
			ImGui.pushStyleColor(ImGuiCol.Text, 0xFF00D8FF);
			ImGui.text(label);
			ImGui.popStyleColor();
		}

		ImGui.sameLine();

		if (isDefault) {
			ImGui.beginDisabled();
		}

		boolean reset = ImGui.smallButton("Reset" + id + "-reset");

		if (isDefault) {
			ImGui.endDisabled();
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip(json(key.defaultValue()));
		}

		if (reset) {
			set(key.defaultValue());
			return Update.FULL;
		}

		return Update.NONE;
	}

	public abstract Update imguiValue();

	public String json(T value) {
		return value.toString();
	}

	public boolean equals(T a, T b) {
		return Objects.equals(a, b);
	}

	public boolean isDefault() {
		return equals(get(), key.defaultValue());
	}

	public void update(VLMinecraftClient mc, boolean full) {
		var value = get();
		mc.getServerData().set(key, value);

		if (full) {
			mc.updateServerDataValue(key, value);
		}
	}
}
