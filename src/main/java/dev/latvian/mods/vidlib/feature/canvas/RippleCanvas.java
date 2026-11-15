package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.joml.Vector2f;

import java.util.List;

public class RippleCanvas {
	public static final ImBoolean ENABLED = new ImBoolean(false);

	public static final Vector2f focusPos = new Vector2f(0F, 0F);
	public static float intensity = 0.3F;
	public static float detail = 50F;
	public static float speed = 4F;

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("ripple"),
		ENABLED::get,
		"VidLibRipple",
		List.of(
			UniformSupplier.of("GameTimeSecond", () -> Canvas.gameTimeSecond),
			UniformSupplier.of("FocusPos", focusPos),
			UniformSupplier.of("Intensity", () -> intensity),
			UniformSupplier.of("Detail", () -> detail),
			UniformSupplier.of("Speed", () -> speed)
		)
	);

	public static void imgui(ImGraphics graphics) {
		ImGui.checkbox("Enabled###enabled", ENABLED);

		ImGui.text("Intensity");

		if (intensity != 0.3F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-intensity")) {
				intensity = 0.3F;
			}
		}

		ImGuiUtils.FLOAT.set(intensity);

		if (ImGui.sliderFloat("###intensity", ImGuiUtils.FLOAT.getData(), 0F, 1F)) {
			intensity = ImGuiUtils.FLOAT.get();
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

		ImGui.text("Detail");

		if (detail != 50F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-detail")) {
				detail = 50F;
			}
		}

		ImGuiUtils.FLOAT.set(detail);

		if (ImGui.sliderFloat("###detail", ImGuiUtils.FLOAT.getData(), 0F, 200F)) {
			detail = ImGuiUtils.FLOAT.get();
		}

		ImGui.text("Speed");

		if (speed != 4F) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.UNDO + "###undo-speed")) {
				speed = 4F;
			}
		}

		ImGuiUtils.FLOAT.set(speed);

		if (ImGui.sliderFloat("###speed", ImGuiUtils.FLOAT.getData(), 0F, 8F)) {
			speed = ImGuiUtils.FLOAT.get();
		}
	}
}
