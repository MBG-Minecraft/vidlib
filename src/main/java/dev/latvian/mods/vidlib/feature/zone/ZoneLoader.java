package dev.latvian.mods.vidlib.feature.zone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.Side;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.JsonReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class ZoneLoader extends JsonReloadListener {
	public static final Map<ResourceKey<Level>, ActiveZones> SERVER_BY_DIMENSION = new IdentityHashMap<>();
	public static final Map<ResourceKey<Level>, ActiveZones> CLIENT_BY_DIMENSION = new IdentityHashMap<>();

	public final Map<ResourceKey<Level>, ActiveZones> byDimension;
	public final boolean serverSide;

	public ZoneLoader(Map<ResourceKey<Level>, ActiveZones> byDimension, boolean serverSide) {
		super("vidlib/zone");
		this.byDimension = byDimension;
		this.serverSide = serverSide;
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
							VidLib.LOGGER.error("Error while parsing zone " + id + "[" + index + "]: " + decoded.error().get());
						} else {
							var zone = decoded.result().orElseThrow();
							container.add(zone);
						}
					}
					index++;
				}

				list.add(container);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while parsing zone container " + id, ex);
			}
		}

		if (serverSide) {
			NeoForge.EVENT_BUS.post(new ZoneEvent.Generate(list));
		}

		list.sort(null);
		byDimension.clear();

		for (var container : list) {
			var zones = byDimension.get(container.dimension);

			if (zones == null) {
				zones = new ActiveZones();
				byDimension.put(container.dimension, zones);
			}

			zones.containers.put(container.id, container);
		}

		var map = new HashMap<ResourceLocation, ZoneContainer>();

		for (var container : list) {
			map.put(container.id, container);
		}

		if (serverSide) {
			ZoneContainer.REGISTRY.update(map);

			for (var entry : byDimension.entrySet()) {
				NeoForge.EVENT_BUS.post(new ZoneEvent.Updated(entry.getKey(), entry.getValue(), Side.SERVER));
			}
		}
	}
}
