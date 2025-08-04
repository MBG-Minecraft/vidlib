package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;

import java.util.Objects;

public class BuilderConfigEntry<T> extends ConfigEntry<T> {
	private final ImBuilder<T> builder;

	public BuilderConfigEntry(String label, DataKey<T> key, ImBuilder<T> builder) {
		super(label, key);
		this.builder = Objects.requireNonNull(builder);
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

	@Override
	public boolean imguiSameLine() {
		return builder.isSmall();
	}

	@Override
	public boolean equals(T a, T b) {
		return builder.equals(a, b);
	}
}
