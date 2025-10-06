package dev.latvian.mods.vidlib.feature.screeneffect.dof;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasUniform;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class DepthOfField {
	public static DepthOfFieldData REPLAY = new DepthOfFieldData(KVector.ZERO, 8F, 16F, 0F, DepthOfFieldShape.SPHERE, DepthOfFieldBlurMode.DEPTH);
	public static DepthOfFieldData OVERRIDE = new DepthOfFieldData(KVector.ZERO, 8F, 16F, 16F, DepthOfFieldShape.SPHERE, DepthOfFieldBlurMode.DEPTH);
	public static final ImBoolean OVERRIDE_ENABLED = new ImBoolean(false);
	public static final ImBoolean DEBUG_ENABLED = new ImBoolean(false);
	public static final Color4ImBuilder DEBUG_NEAR_COLOR = new Color4ImBuilder();
	public static final Color4ImBuilder DEBUG_FAR_COLOR = new Color4ImBuilder();

	static {
		DEBUG_NEAR_COLOR.set(Color.of(0x7F00FF00));
		DEBUG_FAR_COLOR.set(Color.of(0x7FFF0000));
	}

	private static Vec3f uFocusPos = Vec3f.ZERO;
	private static float uFocusRange = 0F;
	private static float uBlurRange = 0F;
	private static float uStrength = 0F;
	private static int uShape = 0;
	private static int uBlurMode = 0;

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("depth_of_field"), builder -> {
		builder.setTickCallback(DepthOfField::tick);
		builder.setDrawSetupCallback(DepthOfField::setup);
		builder.addUniform(CanvasUniform.mat4("InverseViewProjectionMat", () -> ClientMatrices.INVERSE_WORLD));
		builder.addUniform(CanvasUniform.vec3("FocusPos", u -> u.set(uFocusPos)));
		builder.addUniform(CanvasUniform.float1("FocusRange", () -> uFocusRange));
		builder.addUniform(CanvasUniform.float1("BlurRange", () -> uBlurRange));
		builder.addUniform(CanvasUniform.float1("Strength", () -> uStrength));
		builder.addUniform(CanvasUniform.int1("Shape", () -> uShape));
		builder.addUniform(CanvasUniform.int1("BlurMode", () -> uBlurMode));
		builder.addUniform(CanvasUniform.vec4("DebugNearCol", u -> u.set(DEBUG_NEAR_COLOR.build())));
		builder.addUniform(CanvasUniform.vec4("DebugFarCol", u -> u.set(DEBUG_FAR_COLOR.build())));
	});

	public static Vec3 prevFocusPosition = null;
	public static Vec3 focusPosition = null;

	private static void tick(Minecraft mc) {
		prevFocusPosition = focusPosition;
		focusPosition = null;

		if (mc.level == null) {
			return;
		}

		var data = OVERRIDE_ENABLED.get() ? OVERRIDE : REPLAY;

		if (data.strength() <= 0F) {
			return;
		}

		focusPosition = data.focus().get(mc.level.getGlobalContext());

		if (focusPosition == null) {
			focusPosition = mc.gameRenderer.getMainCamera().getPosition();
		}
	}

	private static void setup(Minecraft mc) {
		if (prevFocusPosition == null || focusPosition == null) {
			return;
		}

		var data = OVERRIDE_ENABLED.get() ? OVERRIDE : REPLAY;

		if (data.strength() <= 0F) {
			return;
		}

		var vec = prevFocusPosition.lerp(focusPosition, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));

		CANVAS.markActive();
		var cam = mc.gameRenderer.getMainCamera().getPosition();

		uFocusPos = new Vec3f((float) (vec.x - cam.x), (float) (vec.y - cam.y), (float) (vec.z - cam.z));
		uFocusRange = Math.max(data.focusRange(), 0F);
		uBlurRange = Math.max(data.focusRange(), 0F) + Math.max(data.blurRange(), 0F);
		uStrength = DEBUG_ENABLED.get() ? 0F : Math.max(data.strength() * mc.getEffectScale(), 0F);
		uShape = data.shape().ordinal();
		uBlurMode = data.blurMode().ordinal();
	}
}
