package dev.latvian.mods.vidlib.feature.environment;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.zone.ZoneFluid;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImString;

public class FluidPlanePanel extends Panel {
	public static final FluidPlanePanel INSTANCE = new FluidPlanePanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.WATER, "Fluid Plane", INSTANCE);

	public final ImBoolean enabled;
	public final ZoneFluid[] fluid;
	public final ImFloat y;
	public final ImString search;

	public FluidPlanePanel() {
		super("fluid-plane", "Fluid Plane");
		this.enabled = new ImBoolean(false);
		this.fluid = new ZoneFluid[]{ZoneFluid.OPAQUE_WATER};
		this.y = new ImFloat(60F + 14F / 16F);
		this.search = ImGuiUtils.resizableString();
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			return;
		}

		ImGui.pushItemWidth(-1F);
		var update = ImUpdate.full(ImGui.checkbox("Enabled###enabled", enabled));

		ImGui.text("Fluid");
		update = update.or(graphics.combo("###fluid", fluid, ZoneFluid.MAP.values(), ZoneFluid::id, search));

		ImGui.text("Y Level");
		ImGui.dragFloat("##y", y.getData(), 1F / 16F, -64F, 320F, "%.04f", 0);
		update = update.orItemEdit();

		if (update.isAny()) {
			graphics.mc.player.vl$sessionData().fluidPlane = enabled.get() ? new FluidPlane(fluid[0], y.get()) : null;
		}

		ImGui.popItemWidth();
	}
}
