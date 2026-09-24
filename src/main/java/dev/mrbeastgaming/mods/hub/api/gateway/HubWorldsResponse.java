package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.data.HubWorld;
import net.minecraft.Util;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public record HubWorldsResponse(
	int fetching,
	HubResponseContext ctx,
	List<AvailableWorld> worlds
) {
	public static final int TYPE_DONE = 0;
	public static final int TYPE_NOT_STARTED = 1;
	public static final int TYPE_FETCHING = 2;
	public static final int TYPE_ERROR = 3;

	public record AvailableWorld(
		String uniqueId,
		String sessionType,
		HubProject project,
		HubUser user,
		HubWorld world,
		Optional<URI> iconUrl
	) {
		public static final Codec<AvailableWorld> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("unique_id").forGetter(AvailableWorld::uniqueId),
			Codec.STRING.fieldOf("session_type").forGetter(AvailableWorld::sessionType),
			HubProject.CODEC.fieldOf("project").forGetter(AvailableWorld::project),
			HubUser.CODEC.fieldOf("user").forGetter(AvailableWorld::user),
			HubWorld.CODEC.fieldOf("world").forGetter(AvailableWorld::world),
			HubAPI.URI_BASE_CODEC.optionalFieldOf("icon_url").forGetter(AvailableWorld::iconUrl)
		).apply(i, AvailableWorld::new));

		public boolean search(String search, HubProject project, HubUser user) {
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

	public static final Codec<HubWorldsResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		MapCodec.unit(TYPE_DONE).forGetter(HubWorldsResponse::fetching),
		HubResponseContext.MAP_CODEC.forGetter(HubWorldsResponse::ctx),
		AvailableWorld.CODEC.listOf().fieldOf("worlds").forGetter(HubWorldsResponse::worlds)
	).apply(instance, HubWorldsResponse::new));

	public static HubWorldsResponse CURRENT = new HubWorldsResponse(TYPE_NOT_STARTED, HubResponseContext.EMPTY, List.of());

	public static void update(Consumer<HubWorldsResponse> callback) {
		CURRENT = new HubWorldsResponse(TYPE_FETCHING, CURRENT.ctx, CURRENT.worlds);

		CompletableFuture.runAsync(() -> {
			try {
				CURRENT = HubAPI.MinecraftAPI.getWorlds();
			} catch (Exception ex) {
				ex.printStackTrace();
				CURRENT = new HubWorldsResponse(TYPE_ERROR, HubResponseContext.EMPTY, List.of());
			}

			callback.accept(CURRENT);
		}, Util.nonCriticalIoPool());
	}
}
