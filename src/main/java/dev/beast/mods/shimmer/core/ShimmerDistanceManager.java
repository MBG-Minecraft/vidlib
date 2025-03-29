package dev.beast.mods.shimmer.core;

import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.ChunkPos;

public interface ShimmerDistanceManager {
	default void shimmer$setLoaded(Ticket<ChunkPos> ticket, boolean loaded) {
		throw new NoMixinException(this);
	}
}
