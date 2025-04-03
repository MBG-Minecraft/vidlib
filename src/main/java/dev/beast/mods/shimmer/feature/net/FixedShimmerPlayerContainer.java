package dev.beast.mods.shimmer.feature.net;

import dev.beast.mods.shimmer.core.ShimmerPlayerContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public record FixedShimmerPlayerContainer(Level level, List<? extends Player> players) implements ShimmerPlayerContainer {
	@Override
	public Level shimmer$level() {
		return level;
	}

	@Override
	public List<? extends Player> shimmer$getS2CPlayers() {
		return players;
	}
}
