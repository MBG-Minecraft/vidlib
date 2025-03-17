package dev.beast.mods.shimmer.feature.misc;

public enum PauseType {
	NONE,
	TICK,
	GAME;

	public boolean tick() {
		return this == NONE;
	}
}
