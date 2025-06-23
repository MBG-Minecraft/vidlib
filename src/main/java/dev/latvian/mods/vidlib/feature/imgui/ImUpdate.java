package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;

public enum ImUpdate {
	NONE,
	PARTIAL,
	FULL;

	public static final ImUpdate[] VALUES = values();

	public static ImUpdate full(boolean value) {
		return value ? FULL : NONE;
	}

	public static ImUpdate itemEdit() {
		if (ImGui.isItemDeactivatedAfterEdit()) {
			return ImUpdate.FULL;
		} else if (ImGui.isItemEdited()) {
			return ImUpdate.PARTIAL;
		} else {
			return ImUpdate.NONE;
		}
	}

	public ImUpdate or(ImUpdate other) {
		return VALUES[Math.max(ordinal(), other.ordinal())];
	}

	public ImUpdate or(boolean full) {
		return full ? FULL : this;
	}

	public boolean isAny() {
		return this != NONE;
	}

	public boolean isFull() {
		return this == FULL;
	}
}
