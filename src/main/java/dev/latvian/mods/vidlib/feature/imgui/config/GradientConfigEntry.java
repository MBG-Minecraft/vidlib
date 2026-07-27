package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;

public class GradientConfigEntry extends ConfigEntry<Ref<Gradient>> {
	public final GradientImBuilder builder;

	public GradientConfigEntry(String label, DataKey<Ref<Gradient>> key) {
		super(label, key);
		this.builder = new GradientImBuilder();
	}

	@Override
	public Ref<Gradient> get() {
		if (builder.isValid()) {
			return builder.build();
		} else {
			return Gradient.EMPTY;
		}
	}

	@Override
	public void set(Ref<Gradient> value) {
		builder.set(value);
	}

	@Override
	public ImUpdate imguiValue(ImGraphics graphics) {
		return builder.imgui(graphics);
	}
}
