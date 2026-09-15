package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.HubUserDisplayData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import java.util.List;

public record HubProjectReplaysData(
	Int2ObjectMap<HubUserDisplayData> relevantUsers,
	List<HubReplayData> replays
) {
	public static final Codec<HubProjectReplaysData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubUserDisplayData.INT_MAP_CODEC.optionalFieldOf("relevant_users", Int2ObjectMaps.emptyMap()).forGetter(HubProjectReplaysData::relevantUsers),
		HubReplayData.CODEC.listOf().optionalFieldOf("replays", List.of()).forGetter(HubProjectReplaysData::replays)
	).apply(instance, HubProjectReplaysData::new));
}
