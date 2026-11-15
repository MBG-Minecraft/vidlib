package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vector3dImBuilder;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldBlurMode;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldShape;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import org.joml.Vector3d;
import org.joml.Vector4f;

import java.util.List;

public class DepthOfFieldCanvas {
	public static final ImBoolean ENABLED = new ImBoolean(false);

	public static Ref<KVector> focusPosKVector = KVector.SOURCE;
	public static final Vector3d focusPos = new Vector3d(0.0, 0.0, 0.0);
	public static float focusRange = 8F;
	public static float blurRange = 16F;
	public static float strength = 16F;
	public static DepthOfFieldShape[] shape = {DepthOfFieldShape.SPHERE};
	public static DepthOfFieldBlurMode[] blurMode = {DepthOfFieldBlurMode.DEPTH};
	public static final ImBoolean debug = new ImBoolean(false);
	public static final Vector4f debugNearColor = ARGB.vector4fFromARGB32(0x7F00FF00);
	public static final Vector4f debugFarColor = ARGB.vector4fFromARGB32(0x7FFF0000);

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("depth_of_field"),
		ENABLED::get,
		"VidLibDepthOfField",
		List.of(
			UniformSupplier.of("InverseViewProjectionMat", Canvas.inverseWorldMatrix),
			UniformSupplier.ofVec3("FocusPos", vector -> {
				var mc = Minecraft.getInstance();

				if (focusPosKVector != null && mc.level != null) {
					var ctx = LevelSessionData.of(mc.level).createGlobalContext();
					ctx.sourcePos = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.pos;

					var vec = focusPosKVector.value().get(ctx);

					if (vec != null) {
						vector.set(vec.x, vec.y, vec.z);
					}
				} else {
					vector.set(focusPos);
				}
			}),
			UniformSupplier.of("FocusRange", () -> focusRange),
			UniformSupplier.of("BlurRange", () -> focusRange + blurRange),
			UniformSupplier.of("Strength", () -> debug.get() ? 0F : strength * Minecraft.getInstance().getEffectScale()),
			UniformSupplier.of("Shape", () -> shape[0].ordinal()),
			UniformSupplier.of("BlurMode", () -> blurMode[0].ordinal()),
			UniformSupplier.of("DebugNearCol", debugNearColor),
			UniformSupplier.of("DebugFarCol", debugFarColor)
		)
	);

	public static void imgui(ImGraphics graphics) {
		ImGui.checkbox("Enabled###enabled", ENABLED);

		ImGui.text("Focus Position");

		if (focusPosKVector == null) {
			Vector3dImBuilder.imgui(graphics, focusPos, SelectedPosition.UNIT);

			if (ImGui.smallButton("Focus Self###focus-self")) {
				focusPosKVector = KVector.SOURCE;
			}
		} else if (ImGui.button("Remove Override###remove-focus-override")) {
			focusPosKVector = null;
		}

		ImGui.text("Focus Range");
		ImGuiUtils.FLOAT.set(focusRange);
		ImGui.sliderFloat("###focus-range", ImGuiUtils.FLOAT.getData(), 0F, 30F);
		focusRange = ImGuiUtils.FLOAT.get();

		ImGui.text("Blur Range");
		ImGuiUtils.FLOAT.set(blurRange);
		ImGui.sliderFloat("###blur-range", ImGuiUtils.FLOAT.getData(), 0F, 30F);
		blurRange = ImGuiUtils.FLOAT.get();

		ImGui.text("Strength");
		ImGuiUtils.FLOAT.set(strength);
		ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), 0.01F, 30F);
		strength = ImGuiUtils.FLOAT.get();

		ImGui.text("Shape");
		graphics.combo("###shape", shape, "", DepthOfFieldShape.VALUES);

		ImGui.text("Blur Mode");
		graphics.combo("###blur-mode", blurMode, "", DepthOfFieldBlurMode.VALUES);

		ImGui.checkbox("Debug", debug);

		if (debug.get()) {
			graphics.color4f("###near-color", DepthOfFieldCanvas.debugNearColor);
			ImGui.sameLine();
			ImGui.text("Near Color");

			graphics.color4f("###far-color", DepthOfFieldCanvas.debugFarColor);
			ImGui.sameLine();
			ImGui.text("Far Color");
		}
	}
}
