package dev.beast.mods.shimmer.core;

public interface ShimmerChunkMap {
	default void shimmer$reloadChunks() {
		throw new NoMixinException(this);
	}
}
