package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.feature.registry.GenericVLRegistry;
import net.minecraft.world.scores.PlayerTeam;

public class ServerTeams {
	public static final GenericVLRegistry<String, PlayerTeam> REGISTRY = GenericVLRegistry.createServer();
}
