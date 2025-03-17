package dev.beast.mods.shimmer.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConfigValue<T, C> {
	public final String name;
	public final Codec<C> codec;
	public final Function<T, C> getter;
	public final BiConsumer<T, C> setter;

	public ConfigValue(String name, Codec<C> codec, Function<T, C> getter, BiConsumer<T, C> setter) {
		this.name = name;
		this.codec = codec;
		this.getter = getter;
		this.setter = setter;
	}

	public <O> O encode(T instance, DynamicOps<O> ops) {
		return codec.encodeStart(ops, getter.apply(instance)).getOrThrow();
	}

	public <O> void decode(T instance, DynamicOps<O> ops, O value) {
		setter.accept(instance, codec.parse(ops, value).getOrThrow());
	}
}
