package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.Lazy;
import dev.mrbeastgaming.mods.hub.api.data.HubDBObject;
import dev.mrbeastgaming.mods.hub.api.data.HubDisplayContext;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import java.util.List;

public record HubProjectsResponse(
	HubDisplayContext ctx,
	List<Hex32> projects
) {
	public static final Codec<HubProjectsResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDisplayContext.MAP_CODEC.forGetter(HubProjectsResponse::ctx),
		HubDBObject.idListCodec("projects").forGetter(HubProjectsResponse::projects)
	).apply(instance, HubProjectsResponse::new));

	public static final Lazy<Int2ObjectMap<HubProject>> ALL = Lazy.of(() -> {
		Int2ObjectMap<HubProject> map = new Int2ObjectLinkedOpenHashMap<>();

		try {
			var response = HubAPI.ProjectAPI.getAll();
			var list = response.projects();

			for (var id : list) {
				var project = response.ctx.project(id);

				if (project != null) {
					map.put(project.id().raw(), project);
				}
			}
		} catch (Exception ignored) {
		}

		return map.isEmpty() ? Int2ObjectMaps.emptyMap() : Int2ObjectMaps.unmodifiable(map);
	});
}
