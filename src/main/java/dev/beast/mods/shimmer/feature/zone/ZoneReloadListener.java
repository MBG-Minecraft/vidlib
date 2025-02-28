package dev.beast.mods.shimmer.feature.zone;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.JsonUtils;
import dev.beast.mods.shimmer.util.Side;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
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
				Shimmer.LOGGER.error("Error while reading zone container from " + entry.getKey(), ex);
			}
		}

		return map;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		var list = new ArrayList<ZoneContainer>();

		for (var entry : from.entrySet()) {
			var id = entry.getKey();

			try {
				var json = entry.getValue();
				var dimension = json.has("dimension") ? ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(json.get("dimension").getAsString())) : Level.OVERWORLD;
				var container = new ZoneContainer(id, dimension);
				int index = 0;

				for (var element : json.getAsJsonArray("zones")) {
					if (element.isJsonObject()) {
						var zoneJson = element.getAsJsonObject();
						var decoded = Zone.CODEC.decode(JsonOps.INSTANCE, zoneJson);

						if (decoded.error().isPresent()) {
							Shimmer.LOGGER.error("Error while parsing zone " + id + "[" + index + "]: " + decoded.error().get());
						} else {
							container.add(decoded.result().orElseThrow().getFirst());
						}
					}
					index++;
				}

				list.add(container);
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while parsing zone container " + id, ex);
			}
		}

		ActiveZones.SERVER.update(list);
		NeoForge.EVENT_BUS.post(new ZoneEvent.Updated(ActiveZones.SERVER, Side.SERVER));
	}
}
