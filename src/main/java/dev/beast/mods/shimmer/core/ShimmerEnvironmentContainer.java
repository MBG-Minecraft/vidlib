package dev.beast.mods.shimmer.core;

public interface ShimmerEnvironmentContainer {
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		throw new NoMixinException(this);
	}

	default boolean shimmer$isClient() {
		return shimmer$getEnvironment().shimmer$isClient();
	}
}
