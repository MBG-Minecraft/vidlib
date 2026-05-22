package dev.latvian.mods.vidlib.util.mutable;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface Mutables {
	static <T> Mutable<T> make(Supplier<T> getter, Consumer<T> setter) {
		return new RefMutable<>(getter, setter);
	}

	static MutableInt makeInt(IntSupplier getter, IntConsumer setter) {
		return new RefMutableInt(getter, setter);
	}
}
