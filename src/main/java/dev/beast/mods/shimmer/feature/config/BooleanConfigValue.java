package dev.beast.mods.shimmer.feature.config;

import com.mojang.serialization.Codec;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BooleanConfigValue<T> extends ConfigValue<T, Boolean> {
	public BooleanConfigValue(String name, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
		super(name, Codec.BOOL, getter, setter);
	}
}
