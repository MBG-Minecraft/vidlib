package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;

import java.util.List;

public class ReplayWindowsMenuBarEvent extends ReplayImGuiEventEvent {
	private final List<MenuItem> items;

	public ReplayWindowsMenuBarEvent(ReplayAPI api, ReplaySession session, ImGraphics graphics, List<MenuItem> items) {
		super(api, session, graphics);
		this.items = items;
	}

	public List<MenuItem> getItems() {
		return items;
	}

	public void add(MenuItem item) {
		items.add(item);
	}
}
