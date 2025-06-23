package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

public class ColorImBuilder implements ImBuilder<Color> {
	public String label;
	public final float[] rgba = new float[4];

	public ColorImBuilder(String label) {
		this.label = label;
	}

	@Override
	public void set(Color c) {
		rgba[0] = c.redf();
		rgba[1] = c.greenf();
		rgba[2] = c.bluef();
		rgba[3] = c.alphaf();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.colorPicker4(label, rgba, ImGuiColorEditFlags.PickerHueWheel);
		return ImUpdate.itemEdit();
	}

	@Override
	public Color build() {
		return Color.of(rgba[3], rgba[0], rgba[1], rgba[2]);
	}
}
