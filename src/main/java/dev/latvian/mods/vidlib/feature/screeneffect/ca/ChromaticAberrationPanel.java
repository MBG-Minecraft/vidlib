package dev.latvian.mods.vidlib.feature.screeneffect.ca;

import dev.latvian.mods.vidlib.feature.canvas.AngledChromaticAberrationCanvas;
import dev.latvian.mods.vidlib.feature.canvas.FocusedChromaticAberrationCanvas;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.type.ImBoolean;

public class ChromaticAberrationPanel extends Panel {
	public static final ChromaticAberrationPanel INSTANCE = new ChromaticAberrationPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.ANIMATION, "Chromatic Aberration", INSTANCE);

	public final ImBoolean isAngled;

	private ChromaticAberrationPanel() {
		super("chromatic-aberration", "Chromatic Aberration");
		this.isAngled = new ImBoolean(false);
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);
		ImGui.checkbox("Angled###angled", isAngled);

		if (isAngled.get()) {
			AngledChromaticAberrationCanvas.imgui(graphics);
		} else {
			FocusedChromaticAberrationCanvas.imgui(graphics);
		}

		ImGui.popItemWidth();
	}
}
