package dev.latvian.mods.vidlib.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ValueCapture<T> implements Consumer<T>, Supplier<T> {
	public T value;

	@Override
	public void accept(T t) {
		this.value = t;
	}

	@Override
	public T get() {
		return value;
	}
}
