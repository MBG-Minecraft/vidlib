package dev.latvian.mods.vidlib.feature.imgui;

import net.neoforged.bus.api.Event;

public abstract class AdminPanelEvent extends Event {
	public static class MenuBar extends AdminPanelEvent {
	}

	public static class OpenDropdown extends AdminPanelEvent {
	}

	public static class ConfigDropdown extends AdminPanelEvent {
	}

	public static class DebugDropdown extends AdminPanelEvent {
	}
}
