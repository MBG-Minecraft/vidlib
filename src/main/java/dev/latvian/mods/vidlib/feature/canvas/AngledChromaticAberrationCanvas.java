package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.screeneffect.ca.AngledChromaticAberrationEffect;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector3f;

import java.util.List;

public class AngledChromaticAberrationCanvas {
	public static final ImBoolean ENABLED = new ImBoolean(false);

	public static float strength = 0.1F;
	public static float angle = 0F;
	public static final Vector3f shift = new Vector3f(AngledChromaticAberrationEffect.DEFAULT_SHIFT);

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("angled_chromatic_aberration"),
		ENABLED::get,
		"VidLibAngledChromaticAberration",
		List.of(
			UniformSupplier.of("Strength", () -> strength),
			UniformSupplier.of("Angle", () -> (float) Math.toRadians(angle)),
			UniformSupplier.of("Shift", shift)
		)
	);

	public static void imgui(ImGraphics graphics) {
		ImGui.checkbox("Enabled###enabled", ENABLED);

		ImGui.text("Strength");

		if (strength != 0.1F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-strength")) {
				strength = 0.1F;
			}
		}

		ImGuiUtils.FLOAT.set(strength);
		if (ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), -1F, 1F)) {
			strength = ImGuiUtils.FLOAT.get();
		}

		ImGui.text("Angle");

		if (angle != 0F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-angle")) {
				angle = 0F;
			}
		}

		ImGuiUtils.FLOAT.set(angle);
		if (ImGui.dragFloat("###angle", ImGuiUtils.FLOAT.getData(), 1F, 0F, 360F)) {
			angle = ImGuiUtils.FLOAT.get();
		}

		ImGui.text("Shift");

		if (shift.x != -1F || shift.y != 0F || shift.z != 1F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-shift")) {
				shift.set(AngledChromaticAberrationEffect.DEFAULT_SHIFT);
			}
		}

		shift.get(ImGuiUtils.FLOAT3);

		if (ImGui.dragFloat3("###shift", ImGuiUtils.FLOAT3, 0.01F, -1F, 1F, "%.2f")) {
			shift.set(ImGuiUtils.FLOAT3);
		}
	}
}
