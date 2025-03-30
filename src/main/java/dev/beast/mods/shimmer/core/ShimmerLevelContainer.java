package dev.beast.mods.shimmer.core;

import net.minecraft.world.level.Level;

public interface ShimmerLevelContainer extends ShimmerEnvironmentContainer {
	default Level shimmer$level() {
		throw new NoMixinException(this);
	}

	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return shimmer$level().shimmer$getEnvironment();
	}
}
