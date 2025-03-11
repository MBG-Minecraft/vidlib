package dev.beast.mods.shimmer.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.util.UndashedUuid;
import dev.beast.mods.shimmer.Shimmer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ShimmerCodecs {
	Codec<UUID> UUID = Codec.STRING.xmap(UndashedUuid::fromStringLenient, UndashedUuid::toString);

	Codec<ResourceLocation> SHIMMER_ID = Codec.STRING.xmap(Shimmer::idFromString, Shimmer::idToString);

	Codec<IntList> INT_LIST = Codec.INT.listOf().xmap(IntArrayList::new, Function.identity());
	Codec<IntList> INT_LIST_OR_SELF = Codec.either(Codec.INT, INT_LIST).xmap(either -> either.map(IntArrayList::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list));

	static <T> Codec<List<T>> listOrSelf(Codec<T> elementCodec) {
		return Codec.either(elementCodec, elementCodec.listOf()).xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list));
	}

	Codec<Vec3> VEC_3D = Codec.either(Codec.DOUBLE, Vec3.CODEC).xmap(either -> either.map(d -> new Vec3(d, d, d), Function.identity()), v -> v.x == v.y && v.x == v.z ? Either.left(v.x) : Either.right(v));

	Codec<Unit> UNIT = Codec.unit(Unit.INSTANCE);

	static <K, V> Codec<V> map(Supplier<Map<K, V>> mapGetter, Codec<K> keyCodec, Function<V, K> keyGetter) {
		return keyCodec.flatXmap(k -> {
			var map = mapGetter.get();

			if (map == null) {
				return DataResult.error(() -> "Map is null");
			} else if (map.isEmpty()) {
				return DataResult.error(() -> "Map is empty");
			} else {
				var value = map.get(k);
				return value == null ? DataResult.error(() -> "No value for key " + k) : DataResult.success(value);
			}
		}, v -> DataResult.success(keyGetter.apply(v)));
	}

	static <K, V> Codec<V> map(Map<K, V> map, Codec<K> keyCodec, Function<V, K> keyGetter) {
		Objects.requireNonNull(map, "Map is null");

		return keyCodec.flatXmap(k -> {
			if (map.isEmpty()) {
				return DataResult.error(() -> "Map is empty");
			} else {
				var value = map.get(k);
				return value == null ? DataResult.error(() -> "No value for key " + k) : DataResult.success(value);
			}
		}, v -> DataResult.success(keyGetter.apply(v)));
	}

	static <V> Codec<Optional<V>> optional(Codec<V> codec) {
		return Codec.either(UNIT, codec).xmap(either -> either.map(u -> Optional.empty(), Optional::of), opt -> opt.isPresent() ? Either.right(opt.get()) : Either.left(Unit.INSTANCE));
	}
}
