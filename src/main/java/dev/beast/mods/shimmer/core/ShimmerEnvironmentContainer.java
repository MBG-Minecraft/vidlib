package dev.beast.mods.shimmer.core;

public interface ShimmerEnvironmentContainer {
	default ShimmerMinecraftEnvironment getEnvironment() {
		throw new NoMixinException(this);
	}

	default boolean shimmer$isClient() {
		return getEnvironment().shimmer$isClient();
	}
}
