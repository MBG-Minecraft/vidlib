package dev.beast.mods.shimmer.feature.camera;

import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import net.minecraft.client.player.KeyboardInput;

public interface ControlledCameraOverride extends CameraOverride {
	boolean tick();

	boolean move(KeyboardInput in);

	boolean turn(double yaw, double pitch);
}
