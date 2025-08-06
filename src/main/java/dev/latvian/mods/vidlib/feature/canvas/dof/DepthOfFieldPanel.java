package dev.latvian.mods.vidlib.feature.canvas.dof;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import imgui.ImGui;

public class DepthOfFieldPanel extends AdminPanel {
	public static final DepthOfFieldPanel INSTANCE = new DepthOfFieldPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.APERTURE, "Depth of Field", INSTANCE);

	public final DepthOfFieldDataImBuilder builder;

	private DepthOfFieldPanel() {
		super("depth-of-field", "Depth of Field");
		this.builder = new DepthOfFieldDataImBuilder();
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);

		if (ImGui.checkbox("Override", DepthOfField.OVERRIDE_ENABLED) && DepthOfField.OVERRIDE_ENABLED.get()) {
			builder.set(DepthOfField.OVERRIDE);
		}

		if (DepthOfField.OVERRIDE_ENABLED.get()) {
			if (builder.imgui(graphics).isAny() && builder.isValid()) {
				DepthOfField.OVERRIDE = builder.build();
			}
		}

		ImGui.checkbox("Debug", DepthOfField.DEBUG_ENABLED);

		if (DepthOfField.DEBUG_ENABLED.get()) {
			DepthOfField.DEBUG_NEAR_COLOR.imguiKey(graphics, "Near Color", "near-color");
			DepthOfField.DEBUG_FAR_COLOR.imguiKey(graphics, "Far Color", "far-color");
		}

		if (ImGui.button("Reload Shaders")) {
			MiscClientUtils.reloadShaders(graphics.mc);
		}

		ImGui.popItemWidth();
	}
}

