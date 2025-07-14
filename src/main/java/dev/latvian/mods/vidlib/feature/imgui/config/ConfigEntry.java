package dev.latvian.mods.vidlib.feature.imgui.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

import java.util.Objects;
import java.util.function.IntFunction;

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

	public static ConfigEntry<Color> color(String label, DataKey<Color> key) {
		return new ColorConfigEntry(label, key);
	}

	public static ConfigEntry<Gradient> gradient(String label, DataKey<Gradient> key) {
		return new GradientConfigEntry(label, key);
	}

	public static <E> ConfigEntry<E> ofEnum(String label, DataKey<E> key, IntFunction<E[]> arrayConstructor, E[] options) {
		return new EnumConfigEntry<>(label, key, arrayConstructor, options);
	}

	public static <E> ConfigEntry<E> ofEnum(String label, DataKey<E> key, IntFunction<E[]> arrayConstructor) {
		return new EnumConfigEntry<>(label, key, arrayConstructor, key.getEnumConstants());
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

	public abstract ImUpdate imguiValue(ImGraphics graphics);

	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		boolean sameLine = imguiSameLine();

		if (sameLine) {
			update = update.or(imguiValue(graphics));
			ImGui.sameLine();
			ImGui.alignTextToFramePadding();
		}

		var isDefault = isDefault();

		if (isDefault) {
			ImGui.text(label);
		} else {
			graphics.pushStack();
			graphics.setWarningText();
			ImGui.text(label);
			graphics.popStack();
		}

		ImGui.sameLine();

		if (isDefault) {
			ImGui.beginDisabled();
		}

		if (ImGui.smallButton("Reset" + id + "-reset")) {
			set(key.defaultValue());
			update = ImUpdate.FULL;
		}

		if (isDefault) {
			ImGui.endDisabled();
		} else if (ImGui.isItemHovered()) {
			ImGui.setTooltip(json(graphics.mc.level.jsonOps(), key.defaultValue()));
		}

		if (!sameLine) {
			update = update.or(imguiValue(graphics));
		}

		return update;
	}

	public String json(DynamicOps<JsonElement> ops, T value) {
		return value.toString();
	}

	public boolean equals(T a, T b) {
		return Objects.equals(a, b);
	}

	public boolean isDefault() {
		return equals(get(), key.defaultValue());
	}

	public void update(ImGraphics graphics, boolean full) {
		var value = get();
		graphics.mc.getServerData().set(key, value);

		if (full && !graphics.isClientOnly) {
			graphics.mc.updateServerDataValue(key, value);
		}
	}
}
