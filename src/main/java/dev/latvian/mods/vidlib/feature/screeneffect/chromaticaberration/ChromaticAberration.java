package dev.latvian.mods.vidlib.feature.screeneffect.chromaticaberration;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasFloatUniform;
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

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("chromatic_aberration")).setTickCallback(ChromaticAberration::tick).setDrawSetupCallback(ChromaticAberration::setup);

	public static final CanvasFloatUniform STRENGTH_UNIFORM = CANVAS.floatUniform("Strength");
	public static final CanvasFloatUniform ANGLE_UNIFORM = CANVAS.floatUniform("Angle");
	public static final CanvasFloatUniform FOCUS_UNIFORM = CANVAS.floatUniform("FocusPos");

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
		if (strength <= 0F) {
			return;
		}

		float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		CANVAS.markActive();
		STRENGTH_UNIFORM.set(strength / 10F);
		ANGLE_UNIFORM.set(isAngled ? (float) Math.toRadians(angle) : -1F);
		FOCUS_UNIFORM.set(Mth.lerp(delta, prevFocusPos.x, focusPos.x), Mth.lerp(delta, prevFocusPos.y, focusPos.y));
	}
}
