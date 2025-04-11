package dev.beast.mods.shimmer.core;

import dev.latvian.mods.kmath.Line;
import net.minecraft.world.phys.Vec3;

public interface ShimmerCamera {
	default void shimmer$setPosition(Vec3 pos) {
	}

	default Line ray(double distance) {
		throw new NoMixinException(this);
	}
}
