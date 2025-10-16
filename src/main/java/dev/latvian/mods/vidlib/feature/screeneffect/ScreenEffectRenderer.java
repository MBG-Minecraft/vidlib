package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasUniform;
import net.minecraft.client.Minecraft;

public class ScreenEffectRenderer {
	private static int uCount = 0;

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("screen_effects"), builder -> {
		builder.setTickCallback(ScreenEffectRenderer::tick);
		builder.setDrawSetupCallback(ScreenEffectRenderer::setup);
		builder.addUniform(CanvasUniform.int1("Count", () -> uCount));
		builder.addUniform(CanvasUniform.mat4("InverseViewProjectionMat", () -> ClientMatrices.INVERSE_WORLD));
	});

	private static void tick(Minecraft mc) {
		if (!mc.getPauseType().tick()) {
			return;
		}

		var ctx = mc.level.getGlobalContext();
		var effects = mc.player.vl$sessionData().screenEffects;

		if (!effects.isEmpty()) {
			effects.removeIf(effect -> {
				effect.snap();
				var ectx = ctx.fork(effect.variables);
				float progress = effect.duration <= 1 ? 0F : (effect.tick / (float) effect.duration);
				ectx.progress = (double) progress;
				effect.update(ectx);

				if (!effect.paused) {
					effect.tick++;
				}

				return !effect.paused && effect.duration > 0 && effect.tick >= effect.duration;
			});
		}
	}

	private static void setup(Minecraft mc) {
		var effects = mc.player.vl$sessionData().screenEffects;
		float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		if (!effects.isEmpty()) {
			var texture = ScreenEffectTexture.HOLDER.texture().get();
			uCount = texture.update(effects, delta);
			CANVAS.markActive();
		} else {
			uCount = 0;
		}
	}
}
