package dev.latvian.mods.vidlib.core;

import org.jetbrains.annotations.Nullable;

public interface VLMinecraftEnvironment extends VLMinecraftEnvironmentDataHolder {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return this;
	}

	@Nullable
	default String getServerBrand() {
		return null;
	}
}
