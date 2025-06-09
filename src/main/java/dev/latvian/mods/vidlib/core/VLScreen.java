package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import net.minecraft.world.phys.Vec3;

public interface VLScreen extends CameraOverride {
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
