package dev.latvian.mods.vidlib.feature.screeneffect.fade;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.PositionedColor;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.interpolation.EasingTypeImBuilder;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.util.EasingType;

import java.util.List;
import java.util.Optional;

public class FadeImBuilder implements ImBuilder<Fade> {
	public final GradientImBuilder color = new GradientImBuilder(List.of(new PositionedColor(0F, Color.BLACK)));
	public final ImInt fadeInTicks = new ImInt(20);
	public final ImInt pauseTicks = new ImInt(20);
	public final ImInt fadeOutTicks = new ImInt(20);
	public final ImBuilder<EasingType> fadeInInterpolation = new EasingTypeImBuilder();
	public final ImBuilder<EasingType> fadeOutInterpolation = new EasingTypeImBuilder();

	@Override
	public void set(Fade value) {
		color.set(value.color());
		fadeInTicks.set(value.fadeInTicks());
		pauseTicks.set(value.pauseTicks());
		fadeOutTicks.set(value.fadeOutTicks().orElse(fadeInTicks.get()));
		fadeInInterpolation.set(value.fadeInEase());
		fadeOutInterpolation.set(value.fadeOutEase().orElse(value.fadeInEase()));
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		ImGui.text("Color");
		ImGui.pushID("###color");
		update = update.or(color.imgui(graphics));
		ImGui.popID();

		ImGui.alignTextToFramePadding();
		ImGui.text("Fade In Ticks");
		ImGui.sameLine();
		update = update.or(fadeInInterpolation.imguiKey(graphics, "", "fade-in-interpolation"));
		ImGui.sliderInt("###fade-in-ticks", fadeInTicks.getData(), 0, 60);
		update = update.orItemEdit();

		ImGui.text("Pause Ticks");
		ImGui.sliderInt("###pause-ticks", pauseTicks.getData(), 0, 100);
		update = update.orItemEdit();

		ImGui.alignTextToFramePadding();
		ImGui.text("Fade Out Ticks");
		ImGui.sameLine();
		update = update.or(fadeOutInterpolation.imguiKey(graphics, "", "fade-out-interpolation"));
		ImGui.sliderInt("###fade-out-ticks", fadeOutTicks.getData(), 0, 60);
		update = update.orItemEdit();

		return update;
	}

	@Override
	public boolean isValid() {
		return color.isValid() && fadeInInterpolation.isValid() && fadeOutInterpolation.isValid();
	}

	@Override
	public Fade build() {
		var inInt = fadeInInterpolation.build();
		var outInt = fadeOutInterpolation.build();

		return new Fade(
			color.build(),
			fadeInTicks.get(),
			pauseTicks.get(),
			fadeInTicks.get() == fadeOutTicks.get() ? Optional.empty() : Optional.of(fadeOutTicks.get()),
			inInt,
			outInt.equals(inInt) ? Optional.empty() : Optional.of(outInt)
		);
	}
}
