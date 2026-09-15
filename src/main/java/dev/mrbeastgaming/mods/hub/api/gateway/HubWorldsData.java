package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubUserDisplayData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectDisplayData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record HubWorldsData(
	int fetching,
	List<Entry> entries
) {
	public static final int TYPE_DONE = 0;
	public static final int TYPE_NOT_STARTED = 1;
	public static final int TYPE_FETCHING = 2;
	public static final int TYPE_ERROR = 3;

	public record Entry(
		UUID sessionId,
		String sessionType,
		Optional<HubProjectDisplayData> project,
		Optional<HubUserDisplayData> user,
		HubWorld world,
		String icon
	) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
			KLibCodecs.UUID.fieldOf("session_id").forGetter(Entry::sessionId),
			Codec.STRING.fieldOf("session_type").forGetter(Entry::sessionType),
			HubProjectDisplayData.CODEC.optionalFieldOf("project").forGetter(Entry::project),
			HubUserDisplayData.CODEC.optionalFieldOf("user").forGetter(Entry::user),
			HubWorld.CODEC.fieldOf("world").forGetter(Entry::world),
			Codec.STRING.optionalFieldOf("icon", "").forGetter(Entry::icon)
		).apply(i, Entry::new));

		public void request(UUID requestId) throws Exception {
			HubAPI.MinecraftAPI.postWorldRequest(requestId, sessionId, world.id(), icon);
		}
	}

	public static final Codec<HubWorldsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unit(TYPE_DONE).fieldOf("fetching").forGetter(HubWorldsData::fetching),
		Entry.CODEC.listOf().fieldOf("entries").forGetter(HubWorldsData::entries)
	).apply(instance, HubWorldsData::new));

	public static HubWorldsData CURRENT = new HubWorldsData(TYPE_NOT_STARTED, List.of());

	public static void update() {
		CURRENT = new HubWorldsData(TYPE_FETCHING, List.of());

		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> {
			try {
				CURRENT = HubAPI.MinecraftAPI.getWorlds();
			} catch (Exception ex) {
				ex.printStackTrace();
				CURRENT = new HubWorldsData(TYPE_ERROR, List.of());
			}
		});
	}
}
