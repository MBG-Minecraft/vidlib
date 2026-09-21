package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubUserDisplayData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectDisplayData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.Util;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record HubWorldsData(
	int fetching,
	Int2ObjectMap<HubProjectDisplayData> relevantProjects,
	Int2ObjectMap<HubUserDisplayData> relevantUsers,
	List<AvailableWorld> worlds
) {
	public static final int TYPE_DONE = 0;
	public static final int TYPE_NOT_STARTED = 1;
	public static final int TYPE_FETCHING = 2;
	public static final int TYPE_ERROR = 3;

	public record AvailableWorld(
		String uniqueId,
		String sessionType,
		Hex32 project,
		Hex32 user,
		HubWorld world,
		Optional<URI> iconUrl
	) {
		public static final Codec<AvailableWorld> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("unique_id").forGetter(AvailableWorld::uniqueId),
			Codec.STRING.fieldOf("session_type").forGetter(AvailableWorld::sessionType),
			Hex32.CODEC.optionalFieldOf("project", Hex32.NONE).forGetter(AvailableWorld::project),
			Hex32.CODEC.optionalFieldOf("user", Hex32.NONE).forGetter(AvailableWorld::user),
			HubWorld.CODEC.fieldOf("world").forGetter(AvailableWorld::world),
			HubAPI.URI_BASE_CODEC.optionalFieldOf("icon_url").forGetter(AvailableWorld::iconUrl)
		).apply(i, AvailableWorld::new));

		public boolean search(String search, HubProjectDisplayData project, HubUserDisplayData user) {
			if (search.isEmpty()) {
				return true;
			} else if (world.name().toLowerCase(Locale.ROOT).contains(search)) {
				return true;
			} else if (project.name().toLowerCase(Locale.ROOT).contains(search) || project.productionCode().contains(search)) {
				return true;
			} else if (user.name().toLowerCase(Locale.ROOT).contains(search)) {
				return true;
			} else {
				return uniqueId.toLowerCase(Locale.ROOT).contains(search);
			}
		}
	}

	public static final Codec<HubWorldsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		MapCodec.unit(TYPE_DONE).forGetter(HubWorldsData::fetching),
		HubProjectDisplayData.INT_MAP_CODEC.optionalFieldOf("relevant_projects", Int2ObjectMaps.emptyMap()).forGetter(HubWorldsData::relevantProjects),
		HubUserDisplayData.INT_MAP_CODEC.optionalFieldOf("relevant_users", Int2ObjectMaps.emptyMap()).forGetter(HubWorldsData::relevantUsers),
		AvailableWorld.CODEC.listOf().fieldOf("worlds").forGetter(HubWorldsData::worlds)
	).apply(instance, HubWorldsData::new));

	public static HubWorldsData CURRENT = new HubWorldsData(TYPE_NOT_STARTED, Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap(), List.of());

	public static void update() {
		CURRENT = new HubWorldsData(TYPE_FETCHING, Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap(), List.of());

		CompletableFuture.runAsync(() -> {
			try {
				CURRENT = HubAPI.MinecraftAPI.getWorlds();
			} catch (Exception ex) {
				ex.printStackTrace();
				CURRENT = new HubWorldsData(TYPE_ERROR, Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap(), List.of());
			}
		}, Util.nonCriticalIoPool());
	}
}
