package dev.latvian.mods.vidlib.feature.screeneffect.chromaticaberration;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.renderer.AngledChromaticAberrationEffectRenderer;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.renderer.FocusedChromaticAberrationEffectRenderer;
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

			if (AngledChromaticAberrationEffectRenderer.strength != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-strength")) {
					AngledChromaticAberrationEffectRenderer.strength = 0F;
					AngledChromaticAberrationEffectRenderer.UNIFORMS.update();
				}
			}

			ImGuiUtils.FLOAT.set(AngledChromaticAberrationEffectRenderer.strength);
			if (ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), -1F, 1F)) {
				AngledChromaticAberrationEffectRenderer.strength = ImGuiUtils.FLOAT.get();
				AngledChromaticAberrationEffectRenderer.UNIFORMS.update();
			}

			ImGui.text("Angle");

			if (AngledChromaticAberrationEffectRenderer.angle != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-angle")) {
					AngledChromaticAberrationEffectRenderer.angle = 0F;
					AngledChromaticAberrationEffectRenderer.UNIFORMS.update();
				}
			}

			ImGuiUtils.FLOAT.set(AngledChromaticAberrationEffectRenderer.angle);
			if (ImGui.dragFloat("###angle", ImGuiUtils.FLOAT.getData(), 1F, 0F, 360F)) {
				AngledChromaticAberrationEffectRenderer.angle = ImGuiUtils.FLOAT.get();
				AngledChromaticAberrationEffectRenderer.UNIFORMS.update();
			}
		} else {
			ImGui.text("Strength");

			if (FocusedChromaticAberrationEffectRenderer.strength != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-strength")) {
					FocusedChromaticAberrationEffectRenderer.strength = 0F;
					FocusedChromaticAberrationEffectRenderer.UNIFORMS.update();
				}
			}

			ImGuiUtils.FLOAT.set(FocusedChromaticAberrationEffectRenderer.strength);
			if (ImGui.sliderFloat("###strength", ImGuiUtils.FLOAT.getData(), -1F, 1F)) {
				FocusedChromaticAberrationEffectRenderer.strength = ImGuiUtils.FLOAT.get();
				FocusedChromaticAberrationEffectRenderer.UNIFORMS.update();
			}

			ImGui.text("Focus");

			if (FocusedChromaticAberrationEffectRenderer.focus.x != 0F || FocusedChromaticAberrationEffectRenderer.focus.y != 0F) {
				ImGui.sameLine();

				if (ImGui.smallButton(ImIcons.UNDO + "###undo-focus")) {
					FocusedChromaticAberrationEffectRenderer.focus.set(0F, 0F);
					FocusedChromaticAberrationEffectRenderer.UNIFORMS.update();
				}
			}

			ImGuiUtils.FLOAT2[0] = FocusedChromaticAberrationEffectRenderer.focus.x;
			ImGuiUtils.FLOAT2[1] = FocusedChromaticAberrationEffectRenderer.focus.y;

			if (ImGui.dragFloat2("###focus", ImGuiUtils.FLOAT2, 0.01F, -1F, 1F)) {
				FocusedChromaticAberrationEffectRenderer.focus.set(ImGuiUtils.FLOAT2[0], ImGuiUtils.FLOAT2[1]);
				FocusedChromaticAberrationEffectRenderer.UNIFORMS.update();
			}
		}

		ImGui.popItemWidth();
	}
}
