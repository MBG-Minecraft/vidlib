package dev.latvian.mods.vidlib.feature.screeneffect.dof;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.PositionType;
import imgui.ImGui;

public class DepthOfFieldPanel extends AdminPanel {
	public static final DepthOfFieldPanel INSTANCE = new DepthOfFieldPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.BLUR, "Depth of Field", INSTANCE);

	public final DepthOfFieldDataImBuilder builder;

	private DepthOfFieldPanel() {
		super("depth-of-field", "Depth of Field");
		this.builder = new DepthOfFieldDataImBuilder();
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);
		boolean update = false;

		if (ImGui.checkbox("Override", DepthOfField.OVERRIDE_ENABLED) && DepthOfField.OVERRIDE_ENABLED.get()) {
			var pos = KVector.following(graphics.mc.player, PositionType.EYES);
			builder.set(DepthOfField.OVERRIDE.withFocus(pos));
			builder.focus.set(pos);
			update = true;
		}

		if (DepthOfField.OVERRIDE_ENABLED.get()) {
			if ((builder.imgui(graphics).isAny() || update) && builder.isValid()) {
				DepthOfField.OVERRIDE = builder.build();
			}
		}

		ImGui.checkbox("Debug", DepthOfField.DEBUG_ENABLED);

		if (DepthOfField.DEBUG_ENABLED.get()) {
			DepthOfField.DEBUG_NEAR_COLOR.imguiKey(graphics, "Near Color", "near-color");
			DepthOfField.DEBUG_FAR_COLOR.imguiKey(graphics, "Far Color", "far-color");
		}

		ImGui.popItemWidth();
	}
}

