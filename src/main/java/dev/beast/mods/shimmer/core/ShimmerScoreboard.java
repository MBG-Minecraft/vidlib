package dev.beast.mods.shimmer.core;

import net.minecraft.world.scores.PlayerTeam;

import java.util.Map;

public interface ShimmerScoreboard {
	default Map<String, PlayerTeam> shimmer$getTeams() {
		return Map.of();
	}
}
