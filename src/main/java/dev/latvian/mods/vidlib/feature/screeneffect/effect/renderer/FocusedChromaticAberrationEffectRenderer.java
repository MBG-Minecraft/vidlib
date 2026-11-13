package dev.latvian.mods.vidlib.feature.screeneffect.effect.renderer;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.CustomPostPassUniforms;
import dev.latvian.mods.vidlib.feature.client.UniformSupplier;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectRenderer;
import org.joml.Vector2f;

import java.util.List;

public class FocusedChromaticAberrationEffectRenderer {
	public static float strength = 0F;
	public static final Vector2f focus = new Vector2f(0F, 0F);

	public static final CustomPostPassUniforms UNIFORMS = new CustomPostPassUniforms("FocusedChromaticAberration", List.of(
		UniformSupplier.of("Strength", () -> strength / 10F),
		UniformSupplier.of("Focus", focus)
	));

	public static void build(ScreenEffectRenderer renderer) {
		renderer.addUniforms(UNIFORMS);
		renderer.setPostEffect(ID.vidlib("focused_chromatic_aberration"), () -> strength != 0F);
	}
}
