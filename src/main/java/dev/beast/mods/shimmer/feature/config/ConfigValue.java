package dev.beast.mods.shimmer.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;

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

	public <O> Component valueComponent(DynamicOps<O> ops, C value) {
		return Component.literal(String.valueOf(codec.encodeStart(ops, value).getOrThrow()));
	}

	public <O> O encode(DynamicOps<O> ops, T instance) {
		return codec.encodeStart(ops, getter.apply(instance)).getOrThrow();
	}

	public <O> void decode(DynamicOps<O> ops, T instance, O value) {
		setter.accept(instance, codec.parse(ops, value).getOrThrow());
	}
}
