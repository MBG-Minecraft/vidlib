package dev.beast.mods.shimmer.util.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.beast.mods.shimmer.util.Cast;
import dev.beast.mods.shimmer.util.ShimmerCodecs;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public record SimpleRegistry<V>(
	Map<ResourceLocation, SimpleRegistryType<V>> typeMap,
	Map<V, SimpleRegistryType.Unit<V>> unitTypeMap,
	Codec<SimpleRegistryType<V>> typeCodec,
	StreamCodec<RegistryFriendlyByteBuf, SimpleRegistryType<V>> typeStreamCodec,
	Codec<V> valueCodec,
	MapCodec<V> valueMapCodec,
	StreamCodec<RegistryFriendlyByteBuf, V> valueStreamCodec
) {
	public static <V> SimpleRegistry<V> create(Function<V, SimpleRegistryType<?>> typeGetter) {
		var typeMap = new HashMap<ResourceLocation, SimpleRegistryType<V>>();
		var unitTypeMap = new IdentityHashMap<V, SimpleRegistryType.Unit<V>>();

		var typeCodec = ShimmerCodecs.SHIMMER_ID.xmap(typeMap::get, SimpleRegistryType::id);
		var typeStreamCodec = ShimmerStreamCodecs.SHIMMER_ID.map(typeMap::get, SimpleRegistryType::id);

		Codec<V> unitCodec = ShimmerCodecs.SHIMMER_ID.flatXmap(s -> {
			var value = typeMap.get(s);
			return value instanceof SimpleRegistryType.Unit<V> unit ? DataResult.success(unit.instance()) : DataResult.error(() -> "Value not found");
		}, o -> {
			var unit = unitTypeMap.get(o);
			return unit != null ? DataResult.success(unit.id()) : DataResult.error(() -> "Key not found");
		});

		Codec<V> dispatchCodec = typeCodec.dispatch("type", Cast.to(typeGetter), t -> Cast.to(t.codec));
		Codec<V> valueCodec = Codec.either(unitCodec, dispatchCodec).xmap(either -> either.map(Function.identity(), Function.identity()), v -> unitTypeMap.containsKey(v) ? Either.left(v) : Either.right(v));
		MapCodec<V> valueMapCodec = typeCodec.dispatchMap("type", Cast.to(typeGetter), t -> Cast.to(t.codec));
		StreamCodec<RegistryFriendlyByteBuf, V> valueStreamCodec = typeStreamCodec.dispatch(Cast.to(typeGetter), t -> Cast.to(t.streamCodec));

		return new SimpleRegistry<>(
			typeMap,
			unitTypeMap,
			typeCodec,
			typeStreamCodec,
			valueCodec,
			valueMapCodec,
			valueStreamCodec
		);
	}

	public void register(SimpleRegistryType<? extends V> value) {
		typeMap.put(value.id(), Cast.to(value));

		if (value instanceof SimpleRegistryType.Unit unit) {
			unitTypeMap.put(Cast.to(unit.instance()), unit);
		}
	}

	@Nullable
	public SimpleRegistryType.Unit<V> getType(V value) {
		return unitTypeMap.get(value);
	}
}
