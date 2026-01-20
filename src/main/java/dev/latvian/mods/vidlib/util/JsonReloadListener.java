package dev.latvian.mods.vidlib.util;

import com.google.gson.JsonObject;
import dev.latvian.mods.vidlib.VidLib;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public abstract class JsonReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
	public final String rootPath;
	public final int rootPathOffset;

	public JsonReloadListener(String rootPath) {
		this.rootPath = rootPath;
		this.rootPathOffset = rootPath.length() + 1;
	}

	public boolean onlyRoot() {
		return false;
	}

	private boolean filter(ResourceLocation id) {
		var path = id.getPath();

		if (!path.endsWith(".json")) {
			return false;
		}

		int lastSlash = path.lastIndexOf("/");

		if (onlyRoot()) {
			return lastSlash == rootPathOffset - 1;
		} else {
			return path.charAt(lastSlash + 1) != '_';
		}
	}

	@Override
	public Map<ResourceLocation, JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new Object2ObjectOpenHashMap<ResourceLocation, JsonObject>();
		var allResources = resourceManager.listResources(rootPath, this::filter);

		for (var entry : allResources.entrySet()) {
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
