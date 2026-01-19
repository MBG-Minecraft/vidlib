package dev.latvian.mods.vidlib.feature.environment;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.type.ImBoolean;

public class WorldBorderPanel extends Panel {
	public static final WorldBorderPanel INSTANCE = new WorldBorderPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.SHIELD, "World Border", INSTANCE);

	public final ImBoolean override;
	public final ImBoolean interpolate;
	public final WorldBorderOverrideImBuilder all;
	public final WorldBorderOverrideImBuilder start;
	public final WorldBorderOverrideImBuilder end;

	public WorldBorderPanel() {
		super("world-border", "World Border");
		this.override = new ImBoolean(false);
		this.interpolate = new ImBoolean(false);
		this.all = new WorldBorderOverrideImBuilder(false);
		this.start = new WorldBorderOverrideImBuilder(true);
		this.end = new WorldBorderOverrideImBuilder(true);
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			return;
		}

		ImGui.pushItemWidth(-1F);
		var update = ImUpdate.full(ImGui.checkbox("Override###override", override));

		if (override.get()) {
			if (graphics.isReplay) {
				update = update.or(ImGui.checkbox("Interpolate###interpolate", interpolate));
			}

			if (interpolate.get()) {
				update = update.or(start.imguiKey(graphics, "Start Border", "start"));
				update = update.or(end.imguiKey(graphics, "End Border", "end"));
			} else {
				update = update.or(all.imguiKey(graphics, "Border", "end"));
			}
		}

		if (update.isAny()) {
			if (!override.get()) {
				graphics.session.worldBorderOverrideStart = null;
				graphics.session.worldBorderOverrideEnd = null;
			} else if (graphics.isReplay && interpolate.get()) {
				graphics.session.worldBorderOverrideStart = start.build();
				graphics.session.worldBorderOverrideEnd = end.build();
			} else {
				graphics.session.worldBorderOverrideStart = null;
				graphics.session.worldBorderOverrideEnd = all.build();
			}
		}

		ImGui.popItemWidth();
	}
}
