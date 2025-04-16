package dev.latvian.mods.vidlib.core;

import net.minecraft.world.scores.PlayerTeam;

import java.util.Map;

public interface VLScoreboard {
	default Map<String, PlayerTeam> vl$getTeams() {
		return Map.of();
	}
}
