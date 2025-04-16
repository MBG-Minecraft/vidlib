package dev.latvian.mods.vidlib.util;

import com.google.gson.JsonObject;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public abstract class JsonReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
	public final String rootPath;
	public final int rootPathOffset;

	public JsonReloadListener(String rootPath) {
		this.rootPath = rootPath;
		this.rootPathOffset = rootPath.length() + 1;
	}

	@Override
	protected Map<ResourceLocation, JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, JsonObject>();

		for (var entry : resourceManager.listResources(rootPath, id -> !id.getPath().startsWith("_") && id.getPath().endsWith(".json")).entrySet()) {
			try (var reader = entry.getValue().openAsReader()) {
				var id = entry.getKey().withPath(s -> s.substring(rootPathOffset, s.length() - 5));
				var json = JsonUtils.read(reader);
				map.put(id, json.getAsJsonObject());
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while reading file " + entry.getKey(), ex);
			}
		}

		return map;
	}
}
