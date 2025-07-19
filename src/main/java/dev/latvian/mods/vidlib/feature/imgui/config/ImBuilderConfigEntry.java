package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;

public class ImBuilderConfigEntry<T> extends ConfigEntry<T> {
	public final ImBuilder<T> builder;

	public ImBuilderConfigEntry(String label, DataKey<T> key, ImBuilder<T> builder) {
		super(label, key);
		this.builder = builder;
	}

	@Override
	public T get() {
		return builder.build();
	}

	@Override
	public void set(T value) {
		builder.set(value);
	}

	@Override
	public ImUpdate imguiValue(ImGraphics graphics) {
		return builder.imgui(graphics);
	}
}
