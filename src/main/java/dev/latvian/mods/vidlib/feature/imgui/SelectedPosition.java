package dev.latvian.mods.vidlib.feature.imgui;

public enum SelectedPosition {
	CAMERA(ImIcons.CAMERA.icon),
	ENTITY(ImIcons.ACCOUNT.icon),
	CURSOR(ImIcons.TARGET.icon);

	public final char icon;

	SelectedPosition(char icon) {
		this.icon = icon;
	}
}
