package dev.beast.mods.shimmer.feature.camera;

import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.latvian.mods.kmath.Rotation;
import net.minecraft.world.phys.Vec3;

public record DetachedCamera(Vec3 position, Rotation rotation) implements CameraOverride {
	@Override
	public boolean renderPlayer() {
		return true;
	}

	@Override
	public Vec3 getCameraPosition(float delta) {
		return position;
	}

	@Override
	public Rotation getCameraRotation(float delta, Vec3 cameraPos) {
		return rotation;
	}
}
