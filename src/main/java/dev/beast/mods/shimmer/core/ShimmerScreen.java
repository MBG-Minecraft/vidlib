package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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
	default Vector3f getCameraRotation(float delta, Vec3 cameraPos) {
		return new Vector3f();
	}
}
