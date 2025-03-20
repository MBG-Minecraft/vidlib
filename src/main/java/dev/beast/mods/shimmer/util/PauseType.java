package dev.beast.mods.shimmer.util;

public enum PauseType {
	NONE,
	TICK,
	GAME;

	public boolean tick() {
		return this == NONE;
	}
}
