package dev.latvian.mods.vidlib.core;

public interface VLEnvironmentContainer {
	default VLMinecraftEnvironment getEnvironment() {
		throw new NoMixinException(this);
	}

	default boolean vl$isClient() {
		return getEnvironment().vl$isClient();
	}
}
