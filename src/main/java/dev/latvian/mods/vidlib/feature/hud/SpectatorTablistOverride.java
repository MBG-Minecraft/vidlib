package dev.latvian.mods.vidlib.feature.hud;

import dev.latvian.mods.klib.data.DataType;

/**
 * Represents the hide spectator tablist feature in VidLib.
 * You can hide spectators in the tablist from non-ops, hide them completely, or show them.
 */
public class SpectatorTablistOverride {

	public enum TablistOverrideType {
		HIDE_TO_NON_OPS,
		HIDE,
		SHOW
	}

	public static final DataType<TablistOverrideType> DATA_TYPE = DataType.of(TablistOverrideType.values());

}
