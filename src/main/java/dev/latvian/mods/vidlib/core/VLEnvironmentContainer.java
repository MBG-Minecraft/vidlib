package dev.latvian.mods.vidlib.core;

public interface VLEnvironmentContainer {
	default VLMinecraftEnvironment getEnvironment() {
		throw new NoMixinException(this);
	}

	default boolean isClient() {
		return getEnvironment().isClient();
	}
}
