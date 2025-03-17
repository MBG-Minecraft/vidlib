package dev.beast.mods.shimmer.feature.config;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.math.Range;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FloatConfigValue<T> extends ConfigValue<T, Float> {
	public final Range range;
	public final boolean slider;

	public FloatConfigValue(String name, Range range, boolean slider, Function<T, Float> getter, BiConsumer<T, Float> setter) {
		super(name, Codec.FLOAT, getter, setter);
		this.range = range;
		this.slider = slider;
	}
}
