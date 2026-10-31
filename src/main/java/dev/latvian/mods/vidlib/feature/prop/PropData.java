package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record PropData<P extends Prop, V>(Class<P> origin, String key, DataType<V> type, boolean isRequired, Function<P, V> getter, BiConsumer<P, V> setter) implements PropTypeInfo {
	public static final Comparator<PropData<?, ?>> COMPARATOR = (a, b) -> a.key().compareToIgnoreCase(b.key());

	public static <P extends Prop, V> PropData<P, V> create(Class<P> origin, String key, DataType<V> type, Function<P, V> getter, BiConsumer<P, V> setter) {
		return new PropData<>(origin, key, type, false, getter, setter);
	}

	public static <P extends Prop> PropData<P, Boolean> createBoolean(Class<P> origin, String key, Function<P, Boolean> getter, BiConsumer<P, Boolean> setter) {
		return create(origin, key, DataTypes.BOOL, getter, setter);
	}

	public static <P extends Prop> PropData<P, Integer> createInt(Class<P> origin, String key, Function<P, Integer> getter, BiConsumer<P, Integer> setter) {
		return create(origin, key, DataTypes.VAR_INT, getter, setter);
	}

	public static <P extends Prop> PropData<P, Integer> createInt(Class<P> origin, String key, Function<P, Integer> getter, BiConsumer<P, Integer> setter, int min, int max) {
		return create(origin, key, DataTypes.VAR_INT, getter, setter);
	}

	public static <P extends Prop> PropData<P, Float> createFloat(Class<P> origin, String key, Function<P, Float> getter, BiConsumer<P, Float> setter) {
		return create(origin, key, DataTypes.FLOAT, getter, setter);
	}

	public static <P extends Prop> PropData<P, Float> createFloat(Class<P> origin, String key, Function<P, Float> getter, BiConsumer<P, Float> setter, float min, float max) {
		return create(origin, key, DataTypes.FLOAT, getter, setter);
	}

	public static <P extends Prop> PropData<P, Double> createDouble(Class<P> origin, String key, Function<P, Double> getter, BiConsumer<P, Double> setter) {
		return create(origin, key, DataTypes.DOUBLE, getter, setter);
	}

	public static <P extends Prop> PropData<P, Double> createDouble(Class<P> origin, String key, Function<P, Double> getter, BiConsumer<P, Double> setter, double min, double max) {
		return create(origin, key, DataTypes.DOUBLE, getter, setter);
	}

	public PropData<P, V> required() {
		return new PropData<>(origin, key, type, true, getter, setter);
	}

	@Override
	public @NotNull String toString() {
		return key;
	}
}
