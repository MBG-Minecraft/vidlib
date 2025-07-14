package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;

import java.util.Objects;
import java.util.function.IntFunction;

public class EnumConfigEntry<E> extends ConfigEntry<E> {
	public final E[] data;
	private final E[] options;

	public EnumConfigEntry(String label, DataKey<E> key, IntFunction<E[]> arrayConstructor, E[] options) {
		super(label, key);
		this.data = arrayConstructor.apply(1);
		this.options = Objects.requireNonNull(options);
	}

	@Override
	public E get() {
		return data[0];
	}

	@Override
	public void set(E value) {
		data[0] = value;
	}

	@Override
	public ImUpdate imguiValue(ImGraphics graphics) {
		return graphics.combo(id, "Select...", data, options);
	}

	@Override
	public boolean equals(E a, E b) {
		return a == b;
	}
}
