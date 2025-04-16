package dev.latvian.mods.vidlib.core;

public interface VLChunkMap {
	default void vl$reloadChunks() {
		throw new NoMixinException(this);
	}
}
