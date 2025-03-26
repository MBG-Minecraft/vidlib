package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.util.registry.BasicShimmerRegistry;
import net.minecraft.world.scores.PlayerTeam;

public class ServerTeams {
	public static final BasicShimmerRegistry<String, PlayerTeam> REGISTRY = BasicShimmerRegistry.createServer();
}
