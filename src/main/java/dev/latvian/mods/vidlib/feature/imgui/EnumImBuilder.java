package dev.latvian.mods.vidlib.feature.imgui;

import java.util.function.IntFunction;

public class EnumImBuilder<E> implements ImBuilder<E> {
	public final E[] value;
	public final E[] options;

	public EnumImBuilder(IntFunction<E[]> arrayConstructor, E[] options) {
		this.value = arrayConstructor.apply(1);
		this.options = options;
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
