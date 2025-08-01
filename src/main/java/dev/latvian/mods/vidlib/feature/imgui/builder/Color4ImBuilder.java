package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

public class Color4ImBuilder implements ImBuilder<Color> {
	public static final ImBuilderSupplier<Color> SUPPLIER = Color4ImBuilder::new;

	public final float[] rgba = new float[4];

	@Override
	public void set(Color c) {
		rgba[0] = c.redf();
		rgba[1] = c.greenf();
		rgba[2] = c.bluef();
		rgba[3] = c.alphaf();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.colorEdit4("###color", rgba, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.AlphaPreview | ImGuiColorEditFlags.PickerHueWheel);
		return ImUpdate.itemEdit();
	}

	@Override
	public Color build() {
		return Color.of(rgba[3], rgba[0], rgba[1], rgba[2]);
	}

	@Override
	public boolean isSmall() {
		return true;
	}
}
