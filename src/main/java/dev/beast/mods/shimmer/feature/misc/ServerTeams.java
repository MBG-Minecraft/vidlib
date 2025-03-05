package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.util.registry.RegistryReference;
import net.minecraft.world.scores.PlayerTeam;

public class ServerTeams {
	public static final RegistryReference.Holder<String, PlayerTeam> HOLDER = RegistryReference.createServerHolder();
}
