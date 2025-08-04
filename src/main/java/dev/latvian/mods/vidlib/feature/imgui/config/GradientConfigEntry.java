package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;

public class GradientConfigEntry extends ConfigEntry<Gradient> {
	public final GradientImBuilder builder;

	public GradientConfigEntry(String label, DataKey<Gradient> key) {
		super(label, key);
		this.builder = new GradientImBuilder();
	}

	@Override
	public Gradient get() {
		if (builder.isValid()) {
			return builder.build();
		} else {
			return Color.TRANSPARENT;
		}
	}

	@Override
	public void set(Gradient value) {
		builder.set(value);
	}

	@Override
	public ImUpdate imguiValue(ImGraphics graphics) {
		return builder.imgui(graphics);
	}
}
