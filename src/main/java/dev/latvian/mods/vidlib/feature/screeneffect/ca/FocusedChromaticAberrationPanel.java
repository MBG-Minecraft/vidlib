package dev.latvian.mods.vidlib.feature.screeneffect.ca;

import dev.latvian.mods.vidlib.feature.canvas.FocusedChromaticAberrationCanvas;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

public class FocusedChromaticAberrationPanel extends Panel {
	public static final FocusedChromaticAberrationPanel INSTANCE = new FocusedChromaticAberrationPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.ANIMATION, "Focused Chromatic Aberration", INSTANCE);

	private FocusedChromaticAberrationPanel() {
		super("focused-chromatic-aberration", "Focused Chromatic Aberration");
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);
		FocusedChromaticAberrationCanvas.imgui(graphics);
		ImGui.popItemWidth();
	}
}
