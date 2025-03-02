package dev.beast.mods.shimmer.core;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface ShimmerPlayer extends ShimmerLivingEntity {
	@Override
	default List<? extends Player> shimmer$getPlayers() {
		return List.of((Player) this);
	}

	default ShimmerSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}
}
