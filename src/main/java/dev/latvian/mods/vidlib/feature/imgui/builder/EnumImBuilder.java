package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

public class EnumImBuilder<E> implements ImBuilder<E> {
	public final E[] value;
	public final List<E> options;

	public static EnumImBuilder<Easing> easing(@Nullable Easing defaultValue) {
		return new EnumImBuilder<>(Easing.ARRAY_FACTORY, Easing.VALUES, defaultValue);
	}

	public static EnumImBuilder<Easing> easing() {
		return easing(Easing.LINEAR);
	}

	public EnumImBuilder(IntFunction<E[]> arrayConstructor, List<E> options, @Nullable E defaultValue) {
		this.value = arrayConstructor.apply(1);
		this.options = options;
		this.value[0] = defaultValue;
	}

	public EnumImBuilder(IntFunction<E[]> arrayConstructor, E[] options, @Nullable E defaultValue) {
		this(arrayConstructor, Arrays.asList(options), defaultValue);
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
