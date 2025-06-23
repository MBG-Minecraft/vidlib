package dev.latvian.mods.vidlib.feature.imgui;

import net.neoforged.bus.api.Event;

public abstract class AdminPanelEvent extends Event {
	private final ImGraphics graphics;

	AdminPanelEvent(ImGraphics graphics) {
		this.graphics = graphics;
	}

	public ImGraphics getGraphics() {
		return graphics;
	}

	public static class MenuBar extends AdminPanelEvent {
		MenuBar(ImGraphics graphics) {
			super(graphics);
		}
	}

	public static class OpenDropdown extends AdminPanelEvent {
		OpenDropdown(ImGraphics graphics) {
			super(graphics);
		}
	}

	public static class ConfigDropdown extends AdminPanelEvent {
		ConfigDropdown(ImGraphics graphics) {
			super(graphics);
		}
	}

	public static class DebugDropdown extends AdminPanelEvent {
		DebugDropdown(ImGraphics graphics) {
			super(graphics);
		}
	}
}
