package dev.latvian.mods.vidlib.feature.fade;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.PositionedColor;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import imgui.ImGui;
import imgui.type.ImInt;

import java.util.List;
import java.util.Optional;

public class FadeImBuilder implements ImBuilder<Fade> {
	public final GradientImBuilder color = new GradientImBuilder(List.of(new PositionedColor(0F, Color.BLACK)));
	public final ImInt fadeInTicks = new ImInt(20);
	public final ImInt pauseTicks = new ImInt(20);
	public final ImInt fadeOutTicks = new ImInt(20);
	public final Easing[] fadeInEase = {Easing.LINEAR};
	public final Easing[] fadeOutEase = {Easing.LINEAR};

	@Override
	public void set(Fade value) {
		color.set(value.color());
		fadeInTicks.set(value.fadeInTicks());
		pauseTicks.set(value.pauseTicks());
		fadeOutTicks.set(value.fadeOutTicks().orElse(fadeInTicks.get()));
		fadeInEase[0] = value.fadeInEase();
		fadeOutEase[0] = value.fadeOutEase().orElse(fadeInEase[0]);
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
		update = update.or(graphics.easingCombo("###fade-in-ease", fadeInEase));
		ImGui.sliderInt("###fade-in-ticks", fadeInTicks.getData(), 0, 60);
		update = update.orItemEdit();

		ImGui.text("Pause Ticks");
		ImGui.sliderInt("###pause-ticks", pauseTicks.getData(), 0, 100);
		update = update.orItemEdit();

		ImGui.alignTextToFramePadding();
		ImGui.text("Fade Out Ticks");
		ImGui.sameLine();
		update = update.or(graphics.easingCombo("###fade-out-ease", fadeOutEase));
		ImGui.sliderInt("###fade-out-ticks", fadeOutTicks.getData(), 0, 60);
		update = update.orItemEdit();

		return update;
	}

	@Override
	public boolean isValid() {
		return color.isValid();
	}

	@Override
	public Fade build() {
		return new Fade(
			color.build(),
			fadeInTicks.get(),
			pauseTicks.get(),
			fadeInTicks.get() == fadeOutTicks.get() ? Optional.empty() : Optional.of(fadeOutTicks.get()),
			fadeInEase[0],
			fadeOutEase[0] == fadeInEase[0] ? Optional.empty() : Optional.of(fadeOutEase[0])
		);
	}
}
