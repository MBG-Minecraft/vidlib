package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.AngledChromaticAberrationEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.FocusedChromaticAberrationEffect;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import imgui.ImGui;

import java.util.function.Supplier;

public class ScreenEffectPanel extends AdminPanel {
	public static final ScreenEffectPanel INSTANCE = new ScreenEffectPanel();

	public final int[] duration = {100};

	private ScreenEffectPanel() {
		super("screen-effects", "Screen Effects");
	}

	private void add(ImGraphics graphics, ImIcon icon, String id, String tooltip, Supplier<ScreenEffectInstance> effect) {
		if (effect == null) {
			ImGui.beginDisabled();
		}

		if (ImGui.button(icon + "###add-" + id)) {
			if (effect != null) {
				var inst = effect.get();
				inst.duration = duration[0];
				inst.name = tooltip;
				inst.update(graphics.mc.level.getGlobalContext().fork(0F, inst.variables));
				inst.snap();
				graphics.mc.player.vl$sessionData().screenEffects.add(inst);
			}
		}

		if (effect == null) {
			ImGui.endDisabled();
		}

		ImGuiUtils.hoveredTooltip(tooltip);
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.pushItemWidth(-1F);

		ImGui.alignTextToFramePadding();
		ImGui.text("Add");
		ImGui.sameLine();
		ImGui.sliderInt("###duration", duration, 0, 1200, KMath.NumberFormat.VERY_SHORT.format(duration[0] / 20F) + " s");
		ImGuiUtils.hoveredTooltip("Duration");

		add(graphics, ImIcons.BLUR, "blur", "Blur", null);
		ImGui.sameLine();
		add(graphics, ImIcons.BLUR, "a-blur", "Angled Blur", null);
		ImGui.sameLine();
		add(graphics, ImIcons.BLUR, "z-blur", "Zoom Blur", null);
		ImGui.sameLine();
		add(graphics, ImIcons.BLUR, "dof", "Depth of Field", null);
		ImGui.sameLine();
		add(graphics, ImIcons.ANIMATION, "ripple", "Ripple", null);
		ImGui.sameLine();
		add(graphics, ImIcons.ANIMATION, "screen-shake", "Screen Shake", null);

		add(graphics, ImIcons.PALETTE, "color", "Color", () -> new ColorEffect.Inst(Color.BLACK.withAlpha(0).gradient(Color.BLACK), false));
		ImGui.sameLine();
		add(graphics, ImIcons.STACKS, "f-ca", "Focused Chromatic Aberration", () -> new FocusedChromaticAberrationEffect.Inst(KNumber.of(0.1D), FocusPoint.Screen.CENTER));
		ImGui.sameLine();
		add(graphics, ImIcons.STACKS, "a-ca", "Angled Chromatic Aberration", () -> new AngledChromaticAberrationEffect.Inst(KNumber.of(0.1D), KNumber.ZERO));
		ImGui.sameLine();
		add(graphics, ImIcons.FIRE, "color-burn", "Color Burn", null);
		ImGui.sameLine();
		add(graphics, ImIcons.HIVE, "crt", "CRT", null);

		ImGui.separator();

		var effects = graphics.mc.player.vl$sessionData().screenEffects;
		var delta = graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		for (int i = 0; i < effects.size(); i++) {
			if (i > 0) {
				ImGui.separator();
			}

			var effect = effects.get(i);

			ImGui.pushID(i);
			ImGui.alignTextToFramePadding();
			ImGui.text(effect.name);
			ImGui.sameLine();

			if (graphics.button(ImIcons.DELETE + "###remove", ImColorVariant.RED)) {
				effects.remove(i);
				i--;
			}

			ImGuiUtils.hoveredTooltip("Remove");
			ImGui.sameLine();

			if (graphics.button((effect.paused ? ImIcons.PLAY : ImIcons.PAUSE) + "###pause", ImColorVariant.GRAY)) {
				effect.paused = !effect.paused;
			}

			ImGuiUtils.hoveredTooltip(effect.paused ? "Resume" : "Pause");
			ImGui.sameLine();

			if (effect.paused) {
				ImGuiUtils.FLOAT.set(effect.tick / (float) effect.duration);
				if (ImGui.sliderFloat("###progress", ImGuiUtils.FLOAT.getData(), 0F, 1F)) {
					effect.tick = (int) (ImGuiUtils.FLOAT.get() * effect.duration);
				}
			} else {
				ImGui.progressBar((effect.tick + delta) / (float) effect.duration);

				if (ImGui.isItemClicked()) {
					effect.tick = 0;
				}
			}

			effect.imgui(graphics);

			ImGui.popID();
		}

		ImGui.popItemWidth();
	}
}
