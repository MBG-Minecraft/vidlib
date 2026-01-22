package dev.latvian.mods.vidlib.core;

public interface VLFoodData {
	default float vl$getExhaustionLevel() {
		throw new NoMixinException(this);
	}

	default void vl$setExhaustionLevel(float value) {
		throw new NoMixinException(this);
	}

	default int vl$getTickTimer() {
		throw new NoMixinException(this);
	}

	default void vl$setTickTimer(int value) {
		throw new NoMixinException(this);
	}
}
