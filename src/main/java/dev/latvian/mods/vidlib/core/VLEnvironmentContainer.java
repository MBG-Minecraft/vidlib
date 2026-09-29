package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;

public interface VLEnvironmentContainer {
	default VLMinecraftEnvironment getEnvironment() {
		throw new NoMixinException(this);
	}

	default boolean isClient() {
		return getEnvironment().isClient();
	}
}
