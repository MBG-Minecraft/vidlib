package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import org.jetbrains.annotations.Nullable;

public enum SelectedPosition {
	CAMERA(ImIcons.CAMERA.icon),
	ENTITY(ImIcons.ACCOUNT.icon),
	CURSOR(ImIcons.TARGET.icon);

	public interface Holder {
		@Nullable
		SelectedPosition getSelectedPosition();
	}

	public static final SelectedPosition[] UNIT = new SelectedPosition[1];

	public final char icon;

	SelectedPosition(char icon) {
		this.icon = icon;
	}
}
