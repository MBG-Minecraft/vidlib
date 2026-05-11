package dev.mrbeastgaming.mods.hub.api.project;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Lazy;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import java.util.List;

public record HubProjectsData(
	List<HubProjectData> projects
) {
	public static final Codec<HubProjectsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubProjectData.CODEC.listOf().fieldOf("projects").forGetter(HubProjectsData::projects)
	).apply(instance, HubProjectsData::new));

	public static final Lazy<Int2ObjectMap<HubProjectData>> ALL = Lazy.of(() -> {
		Int2ObjectMap<HubProjectData> map = new Int2ObjectLinkedOpenHashMap<>();
		var pack = HubProjectData.PACK;

		if (pack != null) {
			map.put(pack.id().raw(), pack);
		}

		try {
			var list = HubAPI.apiProjects().projects();

			for (var project : list) {
				if (pack == null || !pack.id().equals(project.id())) {
					map.put(project.id().raw(), project);
				}
			}
		} catch (Exception ignored) {
		}

		return map.isEmpty() ? Int2ObjectMaps.emptyMap() : Int2ObjectMaps.unmodifiable(map);
	});
}
