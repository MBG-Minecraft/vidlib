package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.math.Line;
import net.minecraft.world.phys.Vec3;

public interface ShimmerCamera {
	default void shimmer$setPosition(Vec3 pos) {
	}

	default Line ray(double distance) {
		throw new NoMixinException(this);
	}
}
