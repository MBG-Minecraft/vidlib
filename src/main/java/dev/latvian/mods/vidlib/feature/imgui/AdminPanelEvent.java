package dev.latvian.mods.vidlib.feature.imgui;

import net.neoforged.bus.api.Event;

import java.util.List;

public abstract class AdminPanelEvent extends Event {
	private final ImGraphics graphics;

	AdminPanelEvent(ImGraphics graphics) {
		this.graphics = graphics;
	}

	public ImGraphics getGraphics() {
		return graphics;
	}

	public static abstract class MenuBarEvent extends AdminPanelEvent {
		private final List<MenuItem> items;

		MenuBarEvent(ImGraphics graphics, List<MenuItem> items) {
			super(graphics);
			this.items = items;
		}

		public void add(MenuItem item) {
			this.items.add(item);
		}
	}

	public static class MenuBar extends MenuBarEvent {
		MenuBar(ImGraphics graphics, List<MenuItem> items) {
			super(graphics, items);
		}
	}

	public static class OpenDropdown extends MenuBarEvent {
		OpenDropdown(ImGraphics graphics, List<MenuItem> items) {
			super(graphics, items);
		}
	}

	public static class ConfigDropdown extends MenuBarEvent {
		ConfigDropdown(ImGraphics graphics, List<MenuItem> items) {
			super(graphics, items);
		}
	}

	public static class DebugDropdown extends MenuBarEvent {
		DebugDropdown(ImGraphics graphics, List<MenuItem> items) {
			super(graphics, items);
		}
	}

	public static class ShowDropdown extends MenuBarEvent {
		ShowDropdown(ImGraphics graphics, List<MenuItem> items) {
			super(graphics, items);
		}
	}

	public static class WarpDropdown extends MenuBarEvent {
		WarpDropdown(ImGraphics graphics, List<MenuItem> items) {
			super(graphics, items);
		}
	}
}
