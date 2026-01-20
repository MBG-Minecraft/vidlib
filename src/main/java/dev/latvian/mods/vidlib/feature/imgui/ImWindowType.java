package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;

public enum ImWindowType {
	DOCKED,
	ATTACHED,
	FLOATING;

	public static ImWindowType get(long windowId) {
		if (ImGui.getWindowViewport() == null) {
			return ImWindowType.FLOATING;
		} else if (ImGui.getWindowViewport().getPlatformHandle() != windowId) {
			return ImWindowType.FLOATING;
		} else if (ImGui.isWindowDocked()) {
			return ImWindowType.DOCKED;
		} else {
			return ImWindowType.ATTACHED;
		}
	}
}
