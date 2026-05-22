package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;

public class Color3ImBuilder implements ImBuilder<Color> {
	public static final ImBuilderType<Color> TYPE = Color3ImBuilder::new;
	public static final Color3ImBuilder UNIT = new Color3ImBuilder();

	public final float[] rgb = new float[3];
	private Color color = Color.BLACK;

	@Override
	public void set(Color c) {
		if (c == null) {
			rgb[0] = 0F;
			rgb[1] = 0F;
			rgb[2] = 0F;
			color = Color.BLACK.withAlpha(color.alpha());
		} else {
			rgb[0] = c.redf();
			rgb[1] = c.greenf();
			rgb[2] = c.bluef();
			color = c;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.colorEdit3("###color", rgb, ImGuiColorEditFlags.NoInputs | ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.PickerHueWheel);
		var update = ImUpdate.itemEdit();

		if (update.isAny()) {
			color = Color.of(color.alphaf(), rgb[0], rgb[1], rgb[2]);
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
