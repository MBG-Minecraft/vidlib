package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.math.Rotation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface CameraOverride {
	default double getFOVModifier(double delta) {
		return 1D;
	}

	default boolean renderPlayer() {
		return false;
	}

	default boolean overrideCamera() {
		return true;
	}

	default boolean hideGui() {
		return false;
	}

	@Nullable
	default Biome.Precipitation getWeatherOverride() {
		return null;
	}

	Vec3 getCameraPosition(float delta);

	Rotation getCameraRotation(float delta, Vec3 cameraPos);
}
