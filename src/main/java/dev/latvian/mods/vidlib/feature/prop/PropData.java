package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.data.DataType;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record PropData<P extends Prop, V>(Class<P> origin, String key, DataType<V> type, boolean isRequired, boolean save, boolean sync, Function<P, V> getter, BiConsumer<P, V> setter) implements PropTypeInfo {
	public static final Comparator<PropData<?, ?>> COMPARATOR = (a, b) -> a.key().compareToIgnoreCase(b.key());

	public static <P extends Prop, V> PropData<P, V> create(Class<P> origin, String key, DataType<V> type, boolean save, boolean sync, Function<P, V> getter, BiConsumer<P, V> setter) {
		return new PropData<>(origin, key, type, false, save, sync, getter, setter);
	}

	public static <P extends Prop, V> PropData<P, V> create(Class<P> origin, String key, DataType<V> type, Function<P, V> getter, BiConsumer<P, V> setter) {
		return create(origin, key, type, true, true, getter, setter);
	}

	public PropData<P, V> required() {
		return new PropData<>(origin, key, type, true, save, sync, getter, setter);
	}
}
