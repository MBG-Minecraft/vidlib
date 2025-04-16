package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.util.UndashedUuid;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public interface VLCodecs {
	Codec<UUID> UUID = Codec.STRING.xmap(UndashedUuid::fromStringLenient, UndashedUuid::toString);

	Codec<IntList> INT_LIST = Codec.INT.listOf().xmap(IntArrayList::new, Function.identity());
	Codec<IntList> INT_LIST_OR_SELF = Codec.either(Codec.INT, INT_LIST).xmap(either -> either.map(IntArrayList::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list));

	static <T> Codec<List<T>> listOrSelf(Codec<T> elementCodec) {
		return Codec.either(elementCodec, elementCodec.listOf()).xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list));
	}

	Codec<Vec3> VEC_3 = Codec.either(Codec.DOUBLE, Vec3.CODEC).xmap(either -> either.map(d -> new Vec3(d, d, d), Function.identity()), v -> v.x == v.y && v.x == v.z ? Either.left(v.x) : Either.right(v));

	Codec<ResourceKey<Level>> DIMENSION = ResourceKey.codec(Registries.DIMENSION);

	Codec<Unit> UNIT = Codec.unit(Unit.INSTANCE);

	Codec<SectionPos> SECTION_POS = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 3).map(ints -> SectionPos.of(ints[0], ints[1], ints[2])), pos -> IntStream.of(pos.x(), pos.y(), pos.z()));

	Codec<ShortList> SHORT_LIST = Codec.SHORT.listOf().xmap(ShortArrayList::new, Function.identity());

	Codec<LongSet> LONG_SET = Codec.LONG_STREAM.xmap(LongOpenHashSet::toSet, LongCollection::longStream);

	Codec<SoundSource> SOUND_SOURCE = anyEnumCodec(SoundSource.values(), SoundSource::getName);

	Function<?, String> DEFAULT_NAME_GETTER = o -> o instanceof StringRepresentable s ? s.getSerializedName() : o instanceof Enum<?> e ? e.name().toLowerCase(Locale.ROOT) : o.toString().toLowerCase(Locale.ROOT);

	static <E> Codec<E> anyEnumCodec(E[] enumValues, Function<E, String> nameGetter) {
		var map = new HashMap<String, E>(enumValues.length);

		for (var value : enumValues) {
			map.put(nameGetter.apply(value), value);
		}

		return Codec.STRING.comapFlatMap(s -> {
			var e = map.get(s);

			if (e != null) {
				return DataResult.success(e);
			}

			return DataResult.error(() -> "Unknown enum value: " + s);
		}, nameGetter);
	}

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

	static <V> Codec<Set<V>> set(Codec<V> codec) {
		return Codec.list(codec).xmap(HashSet::new, ArrayList::new);
	}

	static <V> Codec<Set<V>> linkedSet(Codec<V> codec) {
		return Codec.list(codec).xmap(LinkedHashSet::new, ArrayList::new);
	}
}
