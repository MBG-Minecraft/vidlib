package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

public class Color3ImBuilder implements ImBuilder<Color> {
	public static final ImBuilderType<Color> TYPE = Color3ImBuilder::new;
	public static final Color3ImBuilder UNIT = new Color3ImBuilder();

	public final float[] rgba = new float[3];
	private Color color = Color.BLACK;

	@Override
	public void set(Color c) {
		if (c == null) {
			rgba[0] = 0F;
			rgba[1] = 0F;
			rgba[2] = 0F;
			c = Color.BLACK;
		} else {
			rgba[0] = c.redf();
			rgba[1] = c.greenf();
			rgba[2] = c.bluef();
			color = c.alpha() == 255 ? c : c.withAlpha(255);
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.colorEdit3("###color", rgba, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.PickerHueWheel);
		var update = ImUpdate.itemEdit();

		if (update.isAny()) {
			color = Color.of(1F, rgba[0], rgba[1], rgba[2]);
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
