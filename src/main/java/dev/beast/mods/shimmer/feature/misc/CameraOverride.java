package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.math.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface CameraOverride {
	@Nullable
	static CameraOverride get(Minecraft mc) {
		if (mc.screen != null && mc.screen.overrideCamera()) {
			return mc.screen;
		} else if (mc.player != null && mc.player.shimmer$sessionData().cameraOverride != null) {
			return mc.player.shimmer$sessionData().cameraOverride;
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
