package dev.latvian.mods.vidlib.feature.screeneffect.ripple;

import dev.latvian.mods.vidlib.feature.canvas.RippleCanvas;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

public class RipplePanel extends Panel {
	public static final RipplePanel INSTANCE = new RipplePanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.ANIMATION, "Ripple", INSTANCE);

	private RipplePanel() {
		super("ripple", "Ripple");
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);
		RippleCanvas.imgui(graphics);
		ImGui.popItemWidth();
	}
}
