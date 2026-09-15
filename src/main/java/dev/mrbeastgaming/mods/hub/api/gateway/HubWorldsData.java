package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubUserDisplayData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectDisplayData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import java.util.List;
import java.util.UUID;

public record HubWorldsData(
	int fetching,
	Int2ObjectMap<HubProjectDisplayData> relevantProjects,
	Int2ObjectMap<HubUserDisplayData> relevantUsers,
	List<Entry> entries
) {
	public static final int TYPE_DONE = 0;
	public static final int TYPE_NOT_STARTED = 1;
	public static final int TYPE_FETCHING = 2;
	public static final int TYPE_ERROR = 3;

	public record Entry(
		UUID sessionId,
		String sessionType,
		Hex32 project,
		Hex32 user,
		HubWorld world,
		String icon
	) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
			KLibCodecs.UUID.fieldOf("session_id").forGetter(Entry::sessionId),
			Codec.STRING.fieldOf("session_type").forGetter(Entry::sessionType),
			Hex32.CODEC.optionalFieldOf("project", Hex32.NONE).forGetter(Entry::project),
			Hex32.CODEC.optionalFieldOf("user", Hex32.NONE).forGetter(Entry::user),
			HubWorld.CODEC.fieldOf("world").forGetter(Entry::world),
			Codec.STRING.optionalFieldOf("icon", "").forGetter(Entry::icon)
		).apply(i, Entry::new));

		public void request(UUID requestId) throws Exception {
			HubAPI.MinecraftAPI.postWorldRequest(requestId, sessionId, world.id(), icon);
		}
	}

	public static final Codec<HubWorldsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unit(TYPE_DONE).fieldOf("fetching").forGetter(HubWorldsData::fetching),
		HubProjectDisplayData.INT_MAP_CODEC.optionalFieldOf("relevant_projects", Int2ObjectMaps.emptyMap()).forGetter(HubWorldsData::relevantProjects),
		HubUserDisplayData.INT_MAP_CODEC.optionalFieldOf("relevant_users", Int2ObjectMaps.emptyMap()).forGetter(HubWorldsData::relevantUsers),
		Entry.CODEC.listOf().fieldOf("entries").forGetter(HubWorldsData::entries)
	).apply(instance, HubWorldsData::new));

	public static HubWorldsData CURRENT = new HubWorldsData(TYPE_NOT_STARTED, Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap(), List.of());

	public static void update() {
		CURRENT = new HubWorldsData(TYPE_FETCHING, Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap(), List.of());

		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> {
			try {
				CURRENT = HubAPI.MinecraftAPI.getWorlds();
			} catch (Exception ex) {
				ex.printStackTrace();
				CURRENT = new HubWorldsData(TYPE_ERROR, Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap(), List.of());
			}
		});
	}
}
