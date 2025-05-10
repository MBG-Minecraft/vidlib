package dev.latvian.mods.vidlib.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class JsonCodecReloadListener<T> extends JsonReloadListener {
	public final Codec<T> codec;
	public final boolean includeId;

	public JsonCodecReloadListener(String rootPath, Codec<T> codec, boolean includeId) {
		super(rootPath);
		this.codec = codec;
		this.includeId = includeId;
	}

	protected T finalize(T t) {
		return t;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, CompletableFuture<T>>();

		for (var entry : from.entrySet()) {
			var id = entry.getKey();

			try {
				var json = entry.getValue();

				if (includeId) {
					json.addProperty("id", id.toString());
				}

				map.put(id, CompletableFuture.supplyAsync(() -> {
					var decoded = codec.parse(JsonOps.INSTANCE, json);

					if (decoded.error().isPresent()) {
						VidLib.LOGGER.error("Error while parsing " + id.withPath(p -> rootPath + "/" + p) + ": " + decoded.error().get());
						return null;
					} else {
						return finalize(decoded.result().orElseThrow());
					}
				}, Util.backgroundExecutor()));
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while parsing " + id.withPath(p -> rootPath + "/" + p), ex);
			}
		}

		CompletableFuture.allOf(map.values().toArray(new CompletableFuture[0])).join();

		var finalMap = new HashMap<ResourceLocation, T>();

		for (var entry : map.entrySet()) {
			try {
				var v = entry.getValue().get();

				if (v != null) {
					finalMap.put(entry.getKey(), v);
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while parsing " + entry.getKey().withPath(p -> rootPath + "/" + p), ex);
			}
		}

		apply(finalMap);
	}

	protected abstract void apply(Map<ResourceLocation, T> map);
}
