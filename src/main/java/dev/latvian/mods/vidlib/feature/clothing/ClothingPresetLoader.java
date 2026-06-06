package dev.latvian.mods.vidlib.feature.clothing;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.JsonReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class ClothingPresetLoader extends JsonReloadListener {
	public ClothingPresetLoader() {
		super("vidlib/clothing_preset");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		var mc = Minecraft.getInstance();
		var map = new HashMap<ResourceKey<ClothingSet>, ClothingSet>();

		for (var entry : from.entrySet()) {
			try {
				var clothingSet = ClothingSet.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow();
				map.put(ClothingPresets.createId(entry.getKey()), clothingSet);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to load clothing preset " + entry.getKey(), ex);
			}
		}

		ClothingPresets.INSTANCE = new ClothingPresets(map);

		ClothingPresets.IDS.clear();
		ClothingPresets.IDS.addAll(from.keySet());
		ClothingPresets.IDS.sort(null);

		mc.execute(() -> ClothingPresets.ready = true);
	}
}
