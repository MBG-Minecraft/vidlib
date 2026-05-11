package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.HubUserData;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import java.util.ArrayList;
import java.util.List;

public record HubProjectReplaysData(
	List<HubReplayData> replays,
	Int2ObjectMap<HubUserData> relevantUsers
) {
	public static final Codec<HubProjectReplaysData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubReplayData.CODEC.listOf().optionalFieldOf("replays", List.of()).forGetter(HubProjectReplaysData::replays),
		HubUserData.CODEC.listOf().xmap(list -> {
			Int2ObjectMap<HubUserData> map = new Int2ObjectLinkedOpenHashMap<>();

			for (var user : list) {
				map.put(user.id().raw(), user);
			}

			return map;
		}, map -> new ArrayList<>(map.values())).optionalFieldOf("relevant_users", Int2ObjectMaps.emptyMap()).forGetter(HubProjectReplaysData::relevantUsers)
	).apply(instance, HubProjectReplaysData::new));
}
