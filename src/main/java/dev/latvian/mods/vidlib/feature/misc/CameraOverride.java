package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.kmath.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface CameraOverride {
	@Nullable
	static CameraOverride get(Minecraft mc) {
		if (mc.screen != null && mc.screen.overrideCamera()) {
			return mc.screen;
		} else if (mc.player != null && mc.player.vl$sessionData().cameraOverride != null) {
			return mc.player.vl$sessionData().cameraOverride;
		} else {
			return null;
		}
	}

	default double getZoom(double delta) {
		return 1D;
	}

	default boolean renderPlayer() {
		return false;
	}

	default boolean overrideCamera() {
		return true;
	}

	Vec3 getCameraPosition(float delta);

	Rotation getCameraRotation(float delta, Vec3 cameraPos);
}
