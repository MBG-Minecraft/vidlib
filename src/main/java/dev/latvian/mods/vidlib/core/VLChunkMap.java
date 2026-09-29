package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;

public interface VLChunkMap {
	default void vl$reloadChunks() {
		throw new NoMixinException(this);
	}
}
