package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.data.HubDisplayContext;
import dev.mrbeastgaming.mods.hub.api.data.HubReplay;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public record HubProjectReplaysResponse(
	HubDisplayContext ctx,
	List<HubReplay> replays
) {
	public static final Codec<HubProjectReplaysResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDisplayContext.MAP_CODEC.forGetter(HubProjectReplaysResponse::ctx),
		HubReplay.CODEC.listOf().optionalFieldOf("replays", List.of()).forGetter(HubProjectReplaysResponse::replays)
	).apply(instance, HubProjectReplaysResponse::new));

	private static final Map<Hex32, CompletableFuture<HubProjectReplaysResponse>> ALL = new Object2ObjectOpenHashMap<>();

	public static void clearCache() {
		ALL.clear();
	}

	public static CompletableFuture<HubProjectReplaysResponse> get(Hex32 projectId) {
		return ALL.computeIfAbsent(projectId, id -> CompletableFuture.supplyAsync(() -> {
			try {
				return HubAPI.ProjectAPI.getReplays(id);
			} catch (Exception ex) {
				return new HubProjectReplaysResponse(HubDisplayContext.EMPTY, List.of());
			}
		}, Util.nonCriticalIoPool()));
	}
}
