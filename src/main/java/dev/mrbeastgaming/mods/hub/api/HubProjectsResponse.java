package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Lazy;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import java.util.List;

public record HubProjectsResponse(
	HubResponseContext ctx,
	List<HubProject> projects
) {
	public static final Codec<HubProjectsResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubResponseContext.MAP_CODEC.forGetter(HubProjectsResponse::ctx),
		HubProject.CODEC.listOf().fieldOf("projects").forGetter(HubProjectsResponse::projects)
	).apply(instance, HubProjectsResponse::new));

	public static final Lazy<Int2ObjectMap<HubProject>> ALL = Lazy.of(() -> {
		var map = new Int2ObjectLinkedOpenHashMap<HubProject>();

		try {
			var response = HubAPI.ProjectAPI.getAll();

			for (var project : response.projects()) {
				map.put(project.id().raw(), project);
			}
		} catch (Exception ignored) {
		}

		return map.isEmpty() ? Int2ObjectMaps.emptyMap() : Int2ObjectMaps.unmodifiable(map);
	});
}
