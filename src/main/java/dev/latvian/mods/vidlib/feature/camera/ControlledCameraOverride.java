package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import net.minecraft.client.player.KeyboardInput;

public interface ControlledCameraOverride extends CameraOverride {
	boolean tick();

	boolean move(KeyboardInput in);

	boolean turn(double yaw, double pitch);
}
