package dev.beast.mods.shimmer.feature.cutscene;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.JsonReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class CutsceneReloadListener extends JsonReloadListener {
	public CutsceneReloadListener() {
		super("shimmer/cutscene");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, Cutscene>();

		for (var entry : from.entrySet()) {
			var id = entry.getKey();

			try {
				var json = entry.getValue();
				var decoded = Cutscene.CODEC.decode(JsonOps.INSTANCE, json);

				if (decoded.error().isPresent()) {
					Shimmer.LOGGER.error("Error while parsing cutscene " + id + ": " + decoded.error().get());
				} else {
					map.put(id, decoded.result().orElseThrow().getFirst());
				}
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while parsing cutscene " + id, ex);
			}
		}

		Cutscene.SERVER = Map.copyOf(map);
	}
}
