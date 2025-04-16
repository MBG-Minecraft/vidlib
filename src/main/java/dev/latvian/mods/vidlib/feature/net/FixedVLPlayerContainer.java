package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.vidlib.core.VLPlayerContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public record FixedVLPlayerContainer(Level level, List<? extends Player> players) implements VLPlayerContainer {
	@Override
	public Level vl$level() {
		return level;
	}

	@Override
	public List<? extends Player> vl$getS2CPlayers() {
		return players;
	}
}
