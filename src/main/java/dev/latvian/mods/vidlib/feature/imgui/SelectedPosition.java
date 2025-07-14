package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;

public enum SelectedPosition {
	CAMERA(ImIcons.CAMERA.icon),
	ENTITY(ImIcons.ACCOUNT.icon),
	CURSOR(ImIcons.TARGET.icon);

	public final char icon;

	SelectedPosition(char icon) {
		this.icon = icon;
	}
}
