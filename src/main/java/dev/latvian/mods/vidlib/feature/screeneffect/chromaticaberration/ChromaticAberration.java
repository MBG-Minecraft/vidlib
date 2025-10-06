package dev.latvian.mods.vidlib.feature.screeneffect.chromaticaberration;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasUniform;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class ChromaticAberration {
	public static float strength = 0F;
	public static boolean isAngled = false;
	public static float angle = 0F;
	public static KVector focus = null;
	public static Vec2 screenFocus = Vec2.ZERO;
	private static Vec2 uFocusPos = Vec2.ZERO;

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("chromatic_aberration"), builder -> {
		builder.setTickCallback(ChromaticAberration::tick);
		builder.setDrawSetupCallback(ChromaticAberration::setup);
		builder.addUniform(CanvasUniform.float1("Strength", () -> strength / 10F));
		builder.addUniform(CanvasUniform.float1("Angle", () -> isAngled ? (float) Math.toRadians(angle) : -1F));
		builder.addUniform(CanvasUniform.vec2("FocusPos", u -> u.set(uFocusPos)));
	});

	public static Vec2 prevFocusPos = null;
	public static Vec2 focusPos = null;

	private static void tick(Minecraft mc) {
		prevFocusPos = focusPos;
		focusPos = null;

		if (mc.level == null) {
			return;
		}

		if (focus == null) {
			focusPos = screenFocus;
		} else {
			// var pos = focus.get(mc.level.getGlobalContext());
			// resolve world pos
			focusPos = screenFocus;
		}
	}

	private static void setup(Minecraft mc) {
		if (strength == 0F) {
			return;
		}

		float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		CANVAS.markActive();
		uFocusPos = new Vec2(Mth.lerp(delta, prevFocusPos.x, focusPos.x), -Mth.lerp(delta, prevFocusPos.y, focusPos.y));
	}
}
