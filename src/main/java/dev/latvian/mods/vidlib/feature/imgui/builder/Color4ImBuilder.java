package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

public class Color4ImBuilder implements ImBuilder<Color> {
	public static final ImBuilderType<Color> TYPE = Color4ImBuilder::new;
	public static final Color4ImBuilder UNIT = new Color4ImBuilder();

	public final float[] rgba = new float[4];
	private Color color = Color.TRANSPARENT;

	@Override
	public void set(Color c) {
		if (c == null) {
			rgba[0] = 0F;
			rgba[1] = 0F;
			rgba[2] = 0F;
			rgba[3] = 0F;
			color = Color.TRANSPARENT;
		} else {
			rgba[0] = c.redf();
			rgba[1] = c.greenf();
			rgba[2] = c.bluef();
			rgba[3] = c.alphaf();
			color = c;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.colorEdit4("###color", rgba, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.PickerHueWheel);
		var update = ImUpdate.itemEdit();

		if (update.isAny()) {
			color = Color.of(rgba[3], rgba[0], rgba[1], rgba[2]);
		}

		return update;
	}

	@Override
	public Color build() {
		return color;
	}

	@Override
	public boolean isSmall() {
		return true;
	}
}
