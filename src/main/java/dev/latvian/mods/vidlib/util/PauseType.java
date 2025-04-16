package dev.latvian.mods.vidlib.util;

public enum PauseType {
	NONE,
	TICK,
	GAME;

	public boolean tick() {
		return this == NONE;
	}
}
