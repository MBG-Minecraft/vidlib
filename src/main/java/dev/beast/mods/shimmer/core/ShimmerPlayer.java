package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerSessionData;
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

	@Override
	default boolean shimmer$isCreative() {
		return ((Player) this).isCreative();
	}
}
