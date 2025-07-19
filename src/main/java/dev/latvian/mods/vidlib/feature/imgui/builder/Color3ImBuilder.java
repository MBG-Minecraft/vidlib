package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

public class Color3ImBuilder implements ImBuilder<Color> {
	public final float[] rgba = new float[3];

	@Override
	public void set(Color c) {
		rgba[0] = c.redf();
		rgba[1] = c.greenf();
		rgba[2] = c.bluef();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.colorEdit3("###color", rgba, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.PickerHueWheel);
		return ImUpdate.itemEdit();
	}

	@Override
	public Color build() {
		return Color.of(1F, rgba[0], rgba[1], rgba[2]);
	}

	@Override
	public boolean isSmall() {
		return true;
	}
}
