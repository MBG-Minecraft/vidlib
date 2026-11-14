package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.FocusedChromaticAberrationEffect;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class FocusedChromaticAberrationCanvas {
	public static float strength = 0F;
	public static final Vector2f focusPos = new Vector2f(0F, 0F);
	public static final Vector3f shift = new Vector3f(FocusedChromaticAberrationEffect.DEFAULT_SHIFT);

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("focused_chromatic_aberration"),
		() -> strength != 0F,
		"VidLibFocusedChromaticAberration",
		List.of(
			UniformSupplier.of("Strength", () -> strength / 10F),
			UniformSupplier.of("FocusPos", focusPos),
			UniformSupplier.of("Shift", shift)
		)
	);
}
