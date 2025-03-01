package dev.beast.mods.shimmer.core;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public interface ShimmerScreen {
	default double getZoom(double delta) {
		return 1D;
	}

	default boolean renderPlayer() {
		return false;
	}

	default boolean overrideCamera() {
		return false;
	}

	default Vec3 getCameraPosition(float delta) {
		return Vec3.ZERO;
	}

	default Vector3f getCameraRotation(float delta, Vec3 cameraPos) {
		return new Vector3f();
	}
}
