package dev.latvian.mods.vidlib.feature.canvas.dof;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasFloatUniform;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class DepthOfField {
	public static DepthOfFieldData REPLAY = new DepthOfFieldData(KVector.ZERO, 1.5F, 8F, 0F);
	public static DepthOfFieldData OVERRIDE = new DepthOfFieldData(KVector.ZERO, 1.5F, 8F, 10F);
	public static final ImBoolean OVERRIDE_ENABLED = new ImBoolean(false);
	public static final ImBoolean DEBUG_ENABLED = new ImBoolean(false);
	public static final Color4ImBuilder DEBUG_NEAR_COLOR = new Color4ImBuilder();
	public static final Color4ImBuilder DEBUG_FAR_COLOR = new Color4ImBuilder();

	static {
		DEBUG_NEAR_COLOR.set(Color.of(0x7F00FF00));
		DEBUG_FAR_COLOR.set(Color.of(0x7FFF0000));
	}

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("depth_of_field")).setTickCallback(DepthOfField::tick).setDrawSetupCallback(DepthOfField::setup);

	public static final CanvasFloatUniform INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM = CANVAS.mat4Uniform("InverseViewProjectionMat");
	public static final CanvasFloatUniform FOCUS_UNIFORM = CANVAS.vec3Uniform("FocusPos");
	public static final CanvasFloatUniform FOCUS_RANGE_UNIFORM = CANVAS.floatUniform("FocusRange");
	public static final CanvasFloatUniform BLUR_RANGE_UNIFORM = CANVAS.floatUniform("BlurRange");
	public static final CanvasFloatUniform STRENGTH_UNIFORM = CANVAS.floatUniform("Strength");
	public static final CanvasFloatUniform DEBUG_NEAR_COLOR_UNIFORM = CANVAS.floatUniform("DebugNearCol");
	public static final CanvasFloatUniform DEBUG_FAR_COLOR_UNIFORM = CANVAS.floatUniform("DebugFarCol");

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

		var vec = prevFocusPosition.lerp(focusPosition, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));

		CANVAS.markActive();
		var cam = mc.gameRenderer.getMainCamera().getPosition();

		INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM.set(ClientMatrices.INVERSE_WORLD);
		FOCUS_UNIFORM.set((float) (vec.x - cam.x), (float) (vec.y - cam.y), (float) (vec.z - cam.z));
		FOCUS_RANGE_UNIFORM.set(Math.max(data.focusRange(), 0F));
		BLUR_RANGE_UNIFORM.set(Math.max(data.focusRange(), 0F) + Math.max(data.blurRange(), 0F));
		STRENGTH_UNIFORM.set(DEBUG_ENABLED.get() ? 0F : Math.max(data.strength(), 0F));
		DEBUG_NEAR_COLOR_UNIFORM.set(DEBUG_NEAR_COLOR.build());
		DEBUG_FAR_COLOR_UNIFORM.set(DEBUG_FAR_COLOR.build());
	}
}
