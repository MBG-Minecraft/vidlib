package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.AngledChromaticAberrationEffect;
import org.joml.Vector3f;

import java.util.List;

public class AngledChromaticAberrationCanvas {
	public static float strength = 0F;
	public static float angle = 0F;
	public static final Vector3f shift = new Vector3f(AngledChromaticAberrationEffect.DEFAULT_SHIFT);

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("angled_chromatic_aberration"),
		() -> strength != 0F,
		"VidLibAngledChromaticAberration",
		List.of(
			UniformSupplier.of("Strength", () -> strength / 10F),
			UniformSupplier.of("Angle", () -> (float) Math.toRadians(angle)),
			UniformSupplier.of("Shift", shift)
		)
	);
}
