package dev.latvian.mods.vidlib.feature.imgui.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

public class ColorConfigEntry extends ConfigEntry<Color> {
	public final float[] data;

	public ColorConfigEntry(String label, DataKey<Color> key) {
		super(label, key);
		this.data = new float[4];
	}

	@Override
	public Color get() {
		return Color.of(data[3], data[0], data[1], data[2]);
	}

	@Override
	public void set(Color value) {
		data[0] = value.redf();
		data[1] = value.greenf();
		data[2] = value.bluef();
		data[3] = value.alphaf();
	}

	@Override
	public ImUpdate imguiValue(ImGraphics graphics) {
		ImGui.colorEdit4(id, data, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.PickerHueWheel);
		return ImUpdate.itemEdit();
	}

	@Override
	public String json(DynamicOps<JsonElement> ops, Color value) {
		try {
			return Color.CODEC.encodeStart(ops, value).getOrThrow().toString();
		} catch (Exception ignore) {
			return "\"transparent\"";
		}
	}
}
