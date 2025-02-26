package dev.beast.mods.shimmer.feature.zone;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.JsonUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ZoneReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
	@Override
	protected Map<ResourceLocation, JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, JsonObject>();

		for (var entry : resourceManager.listResources("shimmer/zones", id -> id.getPath().endsWith(".json")).entrySet()) {
			try (var reader = entry.getValue().openAsReader()) {
				var id = entry.getKey().withPath(s -> s.substring(14, s.length() - 5));
				var json = JsonUtils.read(reader);
				map.put(id, json.getAsJsonObject());
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while reading zone containers", ex);
			}
		}

		return map;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		ZoneContainer.SERVER = Map.of();
		var map = new HashMap<ResourceLocation, ZoneContainer>();

		for (var entry : from.entrySet()) {
			try {
				var id = entry.getKey();
				var json = entry.getValue();
				var dimension = json.has("dimension") ? ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(json.get("dimension").getAsString())) : Level.OVERWORLD;
				var container = new ZoneContainer(id, dimension);

				for (var element : json.getAsJsonArray("zones")) {
					if (element.isJsonObject()) {
						var zoneJson = element.getAsJsonObject();
						container.add(Zone.CODEC.decode(JsonOps.INSTANCE, zoneJson).result().orElseThrow().getFirst());
					}
				}

				map.put(id, container);
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while parsing zone containers", ex);
			}
		}

		ZoneContainer.SERVER = Map.copyOf(map);
	}
}
