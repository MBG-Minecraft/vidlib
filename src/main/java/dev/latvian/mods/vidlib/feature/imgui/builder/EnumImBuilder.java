package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

public class EnumImBuilder<E> implements ImBuilder<E> {
	public final E[] value;
	public final List<E> options;

	public static EnumImBuilder<Easing> easing() {
		var b = new EnumImBuilder<>(Easing[]::new, Easing.VALUES);
		b.set(Easing.LINEAR);
		return b;
	}

	public EnumImBuilder(IntFunction<E[]> arrayConstructor, List<E> options) {
		this.value = arrayConstructor.apply(1);
		this.options = options;
	}

	public EnumImBuilder(IntFunction<E[]> arrayConstructor, E[] options) {
		this(arrayConstructor, Arrays.asList(options));
	}

	@Override
	public void set(E v) {
		value[0] = v;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return graphics.combo("###enum", "Select...", value, options);
	}

	@Override
	public E build() {
		return value[0];
	}
}
