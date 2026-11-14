package dev.latvian.mods.vidlib.feature.screeneffect.chromaticaberration;

import dev.latvian.mods.vidlib.feature.canvas.AngledChromaticAberrationCanvas;
import dev.latvian.mods.vidlib.feature.canvas.FocusedChromaticAberrationCanvas;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.type.ImBoolean;

public class ChromaticAberrationPanel extends Panel {
	public static final ChromaticAberrationPanel INSTANCE = new ChromaticAberrationPanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.ANIMATION, "Chromatic Aberration", INSTANCE);

	public final ImBoolean isAngled;

	private ChromaticAberrationPanel() {
		super("chromatic-aberration", "Chromatic Aberration");
		this.isAngled = new ImBoolean(false);
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);
		ImGui.checkbox("Angled###angled", isAngled);

		if (isAngled.get()) {
			ImGui.text("Strength");

			if (AngledChromaticAberrationCanvas.strength != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-strength")) {
					AngledChromaticAberrationCanvas.strength = 0F;
					AngledChromaticAberrationCanvas.CANVAS.update();
				}
			}

			ImGuiUtils.FLOAT.set(AngledChromaticAberrationCanvas.strength);
			if (ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), -1F, 1F)) {
				AngledChromaticAberrationCanvas.strength = ImGuiUtils.FLOAT.get();
				AngledChromaticAberrationCanvas.CANVAS.update();
			}

			ImGui.text("Angle");

			if (AngledChromaticAberrationCanvas.angle != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-angle")) {
					AngledChromaticAberrationCanvas.angle = 0F;
					AngledChromaticAberrationCanvas.CANVAS.update();
				}
			}

			ImGuiUtils.FLOAT.set(AngledChromaticAberrationCanvas.angle);
			if (ImGui.dragFloat("###angle", ImGuiUtils.FLOAT.getData(), 1F, 0F, 360F)) {
				AngledChromaticAberrationCanvas.angle = ImGuiUtils.FLOAT.get();
				AngledChromaticAberrationCanvas.CANVAS.update();
			}

			ImGui.text("Shift");

			if (AngledChromaticAberrationCanvas.shift.x != -1F || AngledChromaticAberrationCanvas.shift.y != 0F || AngledChromaticAberrationCanvas.shift.z != 1F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-shift")) {
					AngledChromaticAberrationCanvas.shift.set(-1F, 0F, 1F);
					AngledChromaticAberrationCanvas.CANVAS.update();
				}
			}

			AngledChromaticAberrationCanvas.shift.get(ImGuiUtils.FLOAT3);

			if (ImGui.dragFloat3("###shift", ImGuiUtils.FLOAT3, 0.01F, -1F, 1F, "%.2f")) {
				AngledChromaticAberrationCanvas.shift.set(ImGuiUtils.FLOAT3);
				AngledChromaticAberrationCanvas.CANVAS.update();
			}
		} else {
			ImGui.text("Strength");

			if (FocusedChromaticAberrationCanvas.strength != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-strength")) {
					FocusedChromaticAberrationCanvas.strength = 0F;
					FocusedChromaticAberrationCanvas.CANVAS.update();
				}
			}

			ImGuiUtils.FLOAT.set(FocusedChromaticAberrationCanvas.strength);
			if (ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), 0F, 1F)) {
				FocusedChromaticAberrationCanvas.strength = ImGuiUtils.FLOAT.get();
				FocusedChromaticAberrationCanvas.CANVAS.update();
			}

			ImGui.text("Focus");

			if (FocusedChromaticAberrationCanvas.focusPos.x != 0F || FocusedChromaticAberrationCanvas.focusPos.y != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-focus")) {
					FocusedChromaticAberrationCanvas.focusPos.set(0F, 0F);
					FocusedChromaticAberrationCanvas.CANVAS.update();
				}
			}

			ImGuiUtils.FLOAT2[0] = FocusedChromaticAberrationCanvas.focusPos.x;
			ImGuiUtils.FLOAT2[1] = FocusedChromaticAberrationCanvas.focusPos.y;

			if (ImGui.dragFloat2("###focus", ImGuiUtils.FLOAT2, 0.01F, -1F, 1F)) {
				FocusedChromaticAberrationCanvas.focusPos.set(ImGuiUtils.FLOAT2[0], ImGuiUtils.FLOAT2[1]);
				FocusedChromaticAberrationCanvas.CANVAS.update();
			}

			ImGui.text("Shift");

			if (FocusedChromaticAberrationCanvas.shift.x != 0F || FocusedChromaticAberrationCanvas.shift.y != 0.5F || FocusedChromaticAberrationCanvas.shift.z != 1F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-shift")) {
					FocusedChromaticAberrationCanvas.shift.set(0F, 1F, 2F);
					FocusedChromaticAberrationCanvas.CANVAS.update();
				}
			}

			FocusedChromaticAberrationCanvas.shift.get(ImGuiUtils.FLOAT3);

			if (ImGui.dragFloat3("###shift", ImGuiUtils.FLOAT3, 0.01F, 0F, 2F, "%.2f")) {
				FocusedChromaticAberrationCanvas.shift.set(ImGuiUtils.FLOAT3);
				FocusedChromaticAberrationCanvas.CANVAS.update();
			}
		}

		ImGui.popItemWidth();
	}
}
