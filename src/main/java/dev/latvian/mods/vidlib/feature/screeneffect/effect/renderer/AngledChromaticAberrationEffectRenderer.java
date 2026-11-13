package dev.latvian.mods.vidlib.feature.screeneffect.effect.renderer;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.CustomPostPassUniforms;
import dev.latvian.mods.vidlib.feature.client.UniformSupplier;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectRenderer;

import java.util.List;

public class AngledChromaticAberrationEffectRenderer {
	public static float strength = 0F;
	public static float angle = 0F;

	public static final CustomPostPassUniforms UNIFORMS = new CustomPostPassUniforms("AngledChromaticAberration", List.of(
		UniformSupplier.of("Strength", () -> strength / 10F),
		UniformSupplier.of("Angle", () -> (float) Math.toRadians(angle))
	));

	public static void build(ScreenEffectRenderer renderer) {
		renderer.addUniforms(UNIFORMS);
		renderer.setPostEffect(ID.vidlib("angled_chromatic_aberration"), () -> strength != 0F);
	}
}
