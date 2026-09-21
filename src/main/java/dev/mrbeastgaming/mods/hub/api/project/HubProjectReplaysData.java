package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubUserDisplayData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public record HubProjectReplaysData(
	Int2ObjectMap<HubUserDisplayData> relevantUsers,
	List<HubReplayData> replays
) {
	public static final Codec<HubProjectReplaysData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubUserDisplayData.INT_MAP_CODEC.optionalFieldOf("relevant_users", Int2ObjectMaps.emptyMap()).forGetter(HubProjectReplaysData::relevantUsers),
		HubReplayData.CODEC.listOf().optionalFieldOf("replays", List.of()).forGetter(HubProjectReplaysData::replays)
	).apply(instance, HubProjectReplaysData::new));

	private static final Map<Hex32, CompletableFuture<HubProjectReplaysData>> ALL = new Object2ObjectOpenHashMap<>();

	public static void clearCache() {
		ALL.clear();
	}

	public static CompletableFuture<HubProjectReplaysData> get(Hex32 projectId) {
		return ALL.computeIfAbsent(projectId, id -> CompletableFuture.supplyAsync(() -> {
			try {
				return HubAPI.ProjectAPI.getReplays(HubProjectConfig.INSTANCE.get().projectId());
			} catch (Exception ex) {
				return new HubProjectReplaysData(Int2ObjectMaps.emptyMap(), List.of());
			}
		}, Util.nonCriticalIoPool()));
	}
}
