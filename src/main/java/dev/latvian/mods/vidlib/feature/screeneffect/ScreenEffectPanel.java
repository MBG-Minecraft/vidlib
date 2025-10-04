package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
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
		if (ImGui.button(icon + "###add-" + id)) {
			if (effect != null) {
				var inst = effect.get();
				inst.duration = duration[0];
				graphics.mc.player.vl$sessionData().screenEffects.add(inst);
			}
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

		ImGui.popItemWidth();
	}
}
