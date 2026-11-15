package dev.latvian.mods.vidlib.feature.screeneffect.dof;

import dev.latvian.mods.vidlib.feature.canvas.DepthOfFieldCanvas;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

public class DepthOfFieldPanel extends Panel {
	public static final DepthOfFieldPanel INSTANCE = new DepthOfFieldPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.BLUR, "Depth of Field", INSTANCE);

	private DepthOfFieldPanel() {
		super("depth-of-field", "Depth of Field");
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);
		DepthOfFieldCanvas.imgui(graphics);
		ImGui.popItemWidth();
	}
}

