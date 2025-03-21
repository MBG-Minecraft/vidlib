package dev.beast.mods.shimmer.core;

import net.minecraft.world.level.Level;

public interface ShimmerLevelContainer {
	default Level shimmer$level() {
		throw new NoMixinException(this);
	}
}
