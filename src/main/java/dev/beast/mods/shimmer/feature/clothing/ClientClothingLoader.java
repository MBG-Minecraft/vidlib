package dev.beast.mods.shimmer.feature.clothing;

import com.google.gson.JsonObject;
import dev.beast.mods.shimmer.util.JsonReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ClientClothingLoader extends JsonReloadListener {
	public ClientClothingLoader() {
		super("equipment");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		Clothing.CLOTHING_IDS.clear();

		for (var entry : from.entrySet()) {
			if (entry.getValue().has("layers")) {
				var layers = entry.getValue().getAsJsonObject("layers");

				if (layers.has("humanoid") || layers.has("humanoid_leggings") || layers.has("wings")) {
					Clothing.CLOTHING_IDS.add(entry.getKey());
				}
			}
		}
	}
}
