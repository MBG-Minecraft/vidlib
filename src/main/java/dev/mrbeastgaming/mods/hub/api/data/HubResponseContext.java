package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.HubOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

public record HubResponseContext(
	Int2ObjectMap<HubCountry> relevantCountries,
	Int2ObjectMap<HubUser> relevantUsers,
	Int2ObjectMap<HubProject> relevantProjects,
	Int2ObjectMap<HubTeam> relevantTeams
) {
	public static final HubResponseContext EMPTY = new HubResponseContext(
		Int2ObjectMaps.emptyMap(),
		Int2ObjectMaps.emptyMap(),
		Int2ObjectMaps.emptyMap(),
		Int2ObjectMaps.emptyMap()
	);

	private static <T> MapCodec<Int2ObjectMap<T>> int2ObjectMapCodec(Codec<T> codec, Function<T, Hex32> idGetter, String name) {
		return codec.listOf().xmap(list -> {
			Int2ObjectMap<T> map = new Int2ObjectLinkedOpenHashMap<>();

			for (var user : list) {
				map.put(idGetter.apply(user).raw(), user);
			}

			return map;
		}, map -> new ArrayList<>(map.values())).optionalFieldOf(name, Int2ObjectMaps.emptyMap());
	}

	public static final MapCodec<HubResponseContext> MAP_CODEC = RecordCodecBuilder.<HubResponseContext>mapCodec(i -> i.group(
		int2ObjectMapCodec(HubCountry.DIRECT_CODEC, HubCountry::id, "relevant_countries").forGetter(HubResponseContext::relevantCountries),
		int2ObjectMapCodec(HubUser.DIRECT_CODEC, HubUser::id, "relevant_users").forGetter(HubResponseContext::relevantUsers),
		int2ObjectMapCodec(HubProject.DIRECT_CODEC, HubProject::id, "relevant_projects").forGetter(HubResponseContext::relevantProjects),
		int2ObjectMapCodec(HubTeam.DIRECT_CODEC, HubTeam::id, "relevant_teams").forGetter(HubResponseContext::relevantTeams)
	).apply(i, HubResponseContext::new)).mapResult(new MapCodec.ResultFunction<>() {
		@Override
		public <T> DataResult<HubResponseContext> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<HubResponseContext> result) {
			if (ops instanceof HubOps<?> o && result.isSuccess()) {
				o.ctx = result.getOrThrow();
			}

			return result;
		}

		@Override
		public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, HubResponseContext input, RecordBuilder<T> t) {
			if (ops instanceof HubOps<?> o) {
				o.ctx = input;
			}

			return t;
		}
	});

	public static <ID, T> Codec<T> resolvingCodec(Codec<T> directCodec, Codec<ID> idCodec, Function<T, ID> resolveId, BiFunction<HubResponseContext, ID, @Nullable T> resolveValue) {
		return KLibCodecs.or(new Codec<>() {
			@Override
			public <O> DataResult<O> encode(T input, DynamicOps<O> ops, O prefix) {
				return idCodec.encode(resolveId.apply(input), ops, prefix);
			}

			@Override
			public <O> DataResult<Pair<T, O>> decode(DynamicOps<O> ops, O input) {
				if (ops instanceof HubOps<?> o) {
					// Help
					return idCodec.flatMap(id -> {
						var value = resolveValue.apply(o.ctx, id);

						if (value == null) {
							return DataResult.error(() -> "Could not resolve " + id);
						} else {
							return DataResult.success(value);
						}
					}).decode(ops, input);
				} else {
					return DataResult.error(() -> "Ops " + ops + " aren't HubOps");
				}
			}
		}, directCodec);
	}

	public static <T extends HubDBObject> Codec<T> resolvingCodec(Codec<T> directCodec, BiFunction<HubResponseContext, Hex32, T> resolve) {
		return resolvingCodec(directCodec, Hex32.LENIENT_CODEC, HubDBObject::id, resolve);
	}

	@Nullable
	public HubCountry country(Hex32 id) {
		return relevantCountries.get(id.raw());
	}

	@Nullable
	public HubCountry country(String code) {
		for (var country : relevantCountries.values()) {
			if (country.code().equals(code)) {
				return country;
			}
		}

		return null;
	}

	@Nullable
	public HubUser user(Hex32 id) {
		return relevantUsers.get(id.raw());
	}

	@Nullable
	public HubProject project(Hex32 id) {
		return relevantProjects.get(id.raw());
	}

	@Nullable
	public HubTeam team(Hex32 id) {
		return relevantTeams.get(id.raw());
	}
}
