package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Function;

public record HubDisplayContext(
	Int2ObjectMap<HubCountry> relevantCountries,
	Int2ObjectMap<HubUser> relevantUsers,
	Int2ObjectMap<HubProject> relevantProjects,
	Int2ObjectMap<HubTeam> relevantTeams
) {
	public static final HubDisplayContext EMPTY = new HubDisplayContext(
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

	public static final MapCodec<HubDisplayContext> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		int2ObjectMapCodec(HubCountry.CODEC, HubCountry::id, "relevant_countries").forGetter(HubDisplayContext::relevantCountries),
		int2ObjectMapCodec(HubUser.CODEC, HubUser::id, "relevant_users").forGetter(HubDisplayContext::relevantUsers),
		int2ObjectMapCodec(HubProject.CODEC, HubProject::id, "relevant_projects").forGetter(HubDisplayContext::relevantProjects),
		int2ObjectMapCodec(HubTeam.CODEC, HubTeam::id, "relevant_teams").forGetter(HubDisplayContext::relevantTeams)
	).apply(i, HubDisplayContext::new));

	@Nullable
	public HubCountry country(Hex32 id) {
		return relevantCountries.get(id.raw());
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
