package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.math.Line;
import net.minecraft.world.phys.Vec3;

public interface VLCamera {
	default void vl$setPosition(Vec3 pos) {
	}

	default Line ray(double distance) {
		throw new NoMixinException(this);
	}
}
