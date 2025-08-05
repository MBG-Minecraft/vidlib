package dev.latvian.mods.vidlib.feature.canvas.dof;

import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasFloatUniform;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;

public class DepthOfField {
	public static DepthOfFieldData REPLAY = new DepthOfFieldData(KVector.ZERO, 2F, 4F, 0F);
	public static DepthOfFieldData OVERRIDE = new DepthOfFieldData(KVector.ZERO, 2F, 4F, 1F);
	public static final ImBoolean OVERRIDE_ENABLED = new ImBoolean(false);

	@AutoRegister(Dist.CLIENT)
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("depth_of_field")).setDrawSetupCallback(DepthOfField::setup);

	public static final CanvasFloatUniform FOCUS_UNIFORM = CANVAS.vec3Uniform("DOFFocus");
	public static final CanvasFloatUniform FOCUS_RANGE_UNIFORM = CANVAS.floatUniform("DOFFocusRange");
	public static final CanvasFloatUniform BLUR_RANGE_UNIFORM = CANVAS.floatUniform("DOFBlurRange");
	public static final CanvasFloatUniform STRENGTH_UNIFORM = CANVAS.floatUniform("DOFStrength");
	public static final CanvasFloatUniform INVERSE_WORLD_MATRIX_UNIFORM = CANVAS.mat4Uniform("InverseWorldMat");

	public static void setup(Minecraft mc) {
		if (mc.level == null) {
			return;
		}

		var data = OVERRIDE_ENABLED.get() ? OVERRIDE : REPLAY;

		if (data.strength() <= 0F) {
			return;
		}

		var vec = data.focus().get(mc.level.getGlobalContext());

		if (vec != null && data.focusRange() >= 0F && data.blurRange() >= 0F && data.blurRange() >= data.focusRange()) {
			CANVAS.markActive();

			var camera = mc.gameRenderer.getMainCamera().getPosition();

			FOCUS_UNIFORM.set(
				(float) (vec.x - camera.x),
				(float) (vec.y - camera.y),
				(float) (vec.z - camera.z)
			);

			FOCUS_RANGE_UNIFORM.set(data.focusRange());
			BLUR_RANGE_UNIFORM.set(data.blurRange());
			STRENGTH_UNIFORM.set(data.strength());
			INVERSE_WORLD_MATRIX_UNIFORM.set(ClientMatrices.INVERSE_WORLD);
		}
	}
}
