package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasFloatUniform;
import dev.latvian.mods.vidlib.feature.canvas.CanvasIntUniform;
import net.minecraft.client.Minecraft;

public class ScreenEffectRenderer {
	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("screen_effects")).setTickCallback(ScreenEffectRenderer::tick).setDrawSetupCallback(ScreenEffectRenderer::setup);

	public static final CanvasIntUniform COUNT = CANVAS.intUniform("Count");
	public static final CanvasFloatUniform INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM = CANVAS.mat4Uniform("InverseViewProjectionMat");

	private static void tick(Minecraft mc) {
		var ctx = mc.level.getGlobalContext();
		var effects = mc.player.vl$sessionData().screenEffects;

		if (!effects.isEmpty()) {
			effects.removeIf(effect -> {
				effect.snap();
				effect.update(ctx.fork(effect.duration <= 1 ? 0F : (effect.tick / (float) effect.duration), effect.variables));
				return effect.duration > 0 && effect.tick++ >= effect.duration;
			});
		}
	}

	private static void setup(Minecraft mc) {
		var effects = mc.player.vl$sessionData().screenEffects;
		float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		if (!effects.isEmpty()) {
			var texture = mc.getTextureManager().byPath.get(ScreenEffectTexture.ID);

			if (texture == null) {
				texture = new ScreenEffectTexture();
				mc.getTextureManager().register(ScreenEffectTexture.ID, texture);
			}

			((ScreenEffectTexture) texture).update(effects, delta);
			COUNT.set(effects.size());
			INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM.set(ClientMatrices.INVERSE_WORLD);
			CANVAS.markActive();
		}
	}
}
