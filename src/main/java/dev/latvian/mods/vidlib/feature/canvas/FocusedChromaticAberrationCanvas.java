package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.screeneffect.ca.FocusedChromaticAberrationEffect;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public class FocusedChromaticAberrationCanvas {
	public static final ImBoolean ENABLED = new ImBoolean(false);

	public static float strength = 0.1F;
	public static final Vector2f focusPos = new Vector2f(0F, 0F);
	public static final Vector3f shift = new Vector3f(FocusedChromaticAberrationEffect.DEFAULT_SHIFT);

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("focused_chromatic_aberration"),
		ENABLED::get,
		"VidLibFocusedChromaticAberration",
		List.of(
			UniformSupplier.of("Strength", () -> strength),
			UniformSupplier.of("FocusPos", focusPos),
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
		if (ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), 0F, 1F)) {
			strength = ImGuiUtils.FLOAT.get();
		}

		ImGui.text("Focus");

		if (focusPos.x != 0F || focusPos.y != 0F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-focus")) {
				focusPos.set(0F, 0F);
			}
		}

		ImGuiUtils.FLOAT2[0] = focusPos.x;
		ImGuiUtils.FLOAT2[1] = focusPos.y;

		if (ImGui.dragFloat2("###focus", ImGuiUtils.FLOAT2, 0.01F, -1F, 1F)) {
			focusPos.set(ImGuiUtils.FLOAT2[0], ImGuiUtils.FLOAT2[1]);
		}

		ImGui.text("Shift");

		if (shift.x != 0F || shift.y != 0.5F || shift.z != 1F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-shift")) {
				shift.set(FocusedChromaticAberrationEffect.DEFAULT_SHIFT);
			}
		}

		shift.get(ImGuiUtils.FLOAT3);

		if (ImGui.dragFloat3("###shift", ImGuiUtils.FLOAT3, 0.01F, 0F, 2F, "%.2f")) {
			shift.set(ImGuiUtils.FLOAT3);
		}
	}
}
