package dev.latvian.mods.vidlib.util;

import java.util.function.Supplier;

public record UnitSupplier<T>(T value) implements Supplier<T> {
	@Override
	public T get() {
		return value;
	}
}
