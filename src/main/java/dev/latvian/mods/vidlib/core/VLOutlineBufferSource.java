package dev.latvian.mods.vidlib.core;

public interface VLOutlineBufferSource {
	default void vl$setPlayer(boolean player) {
		throw new NoMixinException(this);
	}
}
