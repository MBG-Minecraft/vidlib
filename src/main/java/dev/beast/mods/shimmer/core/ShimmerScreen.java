package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.beast.mods.shimmer.math.Rotation;
import net.minecraft.world.phys.Vec3;

public interface ShimmerScreen extends CameraOverride {
	@Override
	default boolean overrideCamera() {
		return false;
	}

	@Override
	default Vec3 getCameraPosition(float delta) {
		return Vec3.ZERO;
	}

	@Override
	default Rotation getCameraRotation(float delta, Vec3 cameraPos) {
		return Rotation.NONE;
	}
}
