package dev.latvian.mods.vidlib.feature.screeneffect.ca;

import dev.latvian.mods.vidlib.feature.canvas.AngledChromaticAberrationCanvas;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

public class AngledChromaticAberrationPanel extends Panel {
	public static final AngledChromaticAberrationPanel INSTANCE = new AngledChromaticAberrationPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.ANIMATION, "Angled Chromatic Aberration", INSTANCE);

	private AngledChromaticAberrationPanel() {
		super("angled-chromatic-aberration", "Angled Chromatic Aberration");
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);
		AngledChromaticAberrationCanvas.imgui(graphics);
		ImGui.popItemWidth();
	}
}
