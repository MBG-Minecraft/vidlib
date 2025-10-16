package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.IntImBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record PropData<P extends Prop, V>(Class<P> origin, String key, DataType<V> type, boolean isRequired, Function<P, V> getter, BiConsumer<P, V> setter, @Nullable ImBuilderType<V> imBuilder) implements PropTypeInfo {
	public static final Comparator<PropData<?, ?>> COMPARATOR = (a, b) -> a.key().compareToIgnoreCase(b.key());

	public static <P extends Prop, V> PropData<P, V> create(Class<P> origin, String key, DataType<V> type, Function<P, V> getter, BiConsumer<P, V> setter, @Nullable ImBuilderType<V> imBuilder) {
		return new PropData<>(origin, key, type, false, getter, setter, imBuilder);
	}

	public static <P extends Prop> PropData<P, Boolean> createBoolean(Class<P> origin, String key, Function<P, Boolean> getter, BiConsumer<P, Boolean> setter) {
		return create(origin, key, DataTypes.BOOL, getter, setter, BooleanImBuilder.TYPE);
	}

	public static <P extends Prop> PropData<P, Integer> createInt(Class<P> origin, String key, Function<P, Integer> getter, BiConsumer<P, Integer> setter) {
		return create(origin, key, DataTypes.VAR_INT, getter, setter, IntImBuilder.TYPE_1M);
	}

	public static <P extends Prop> PropData<P, Integer> createInt(Class<P> origin, String key, Function<P, Integer> getter, BiConsumer<P, Integer> setter, int min, int max) {
		return create(origin, key, DataTypes.VAR_INT, getter, setter, () -> new IntImBuilder(min, max));
	}

	public static <P extends Prop> PropData<P, Float> createFloat(Class<P> origin, String key, Function<P, Float> getter, BiConsumer<P, Float> setter) {
		return create(origin, key, DataTypes.FLOAT, getter, setter, FloatImBuilder.TYPE);
	}

	public static <P extends Prop> PropData<P, Float> createFloat(Class<P> origin, String key, Function<P, Float> getter, BiConsumer<P, Float> setter, float min, float max) {
		return create(origin, key, DataTypes.FLOAT, getter, setter, FloatImBuilder.type(min, max));
	}

	public PropData<P, V> required() {
		return new PropData<>(origin, key, type, true, getter, setter, imBuilder);
	}

	@Override
	public @NotNull String toString() {
		return key;
	}
}
