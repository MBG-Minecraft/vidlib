package dev.beast.mods.shimmer.feature.zone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.JsonReloadListener;
import dev.beast.mods.shimmer.util.Side;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

public class ZoneLoader extends JsonReloadListener {
	public static final ActiveZones ALL = new ActiveZones();
	public static final Map<ResourceKey<Level>, ActiveZones> BY_DIMENSION = new IdentityHashMap<>();

	public ZoneLoader() {
		super("shimmer/zone");
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

				if (json.get("tags") instanceof JsonArray array) {
					for (var element : array) {
						container.tags.add(element.getAsString());
					}
				}

				if (json.has("priority")) {
					container.priority = json.get("priority").getAsInt();
				}

				for (var element : json.getAsJsonArray("zones")) {
					if (element.isJsonObject()) {
						var zoneJson = element.getAsJsonObject();
						var decoded = Zone.CODEC.parse(JsonOps.INSTANCE, zoneJson);

						if (decoded.error().isPresent()) {
							Shimmer.LOGGER.error("Error while parsing zone " + id + "[" + index + "]: " + decoded.error().get());
						} else {
							container.add(decoded.result().orElseThrow());
						}
					}
					index++;
				}

				list.add(container);
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while parsing zone container " + id, ex);
			}
		}

		list.sort(null);
		ALL.update(list);
		BY_DIMENSION.clear();

		for (var container : list) {
			var zones = BY_DIMENSION.get(container.dimension);

			if (zones == null) {
				zones = new ActiveZones();
				BY_DIMENSION.put(container.dimension, zones);
			}

			zones.containers.put(container.id, container);
		}

		NeoForge.EVENT_BUS.post(new ZoneEvent.AllUpdated(ALL, Side.SERVER));

		for (var entry : BY_DIMENSION.entrySet()) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.Updated(entry.getKey(), entry.getValue(), Side.SERVER));
		}
	}
}
