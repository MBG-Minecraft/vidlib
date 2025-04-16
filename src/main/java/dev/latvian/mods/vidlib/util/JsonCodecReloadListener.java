package dev.latvian.mods.vidlib.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public abstract class JsonCodecReloadListener<T> extends JsonReloadListener {
	public final Codec<T> codec;
	public final boolean includeId;

	public JsonCodecReloadListener(String rootPath, Codec<T> codec, boolean includeId) {
		super(rootPath);
		this.codec = codec;
		this.includeId = includeId;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonObject> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, T>();

		for (var entry : from.entrySet()) {
			var id = entry.getKey();

			try {
				var json = entry.getValue();

				if (includeId) {
					json.addProperty("id", id.toString());
				}

				var decoded = codec.parse(JsonOps.INSTANCE, json);

				if (decoded.error().isPresent()) {
					VidLib.LOGGER.error("Error while parsing " + id.withPath(p -> rootPath + "/" + p) + ": " + decoded.error().get());
				} else {
					map.put(id, decoded.result().orElseThrow());
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while parsing " + id.withPath(p -> rootPath + "/" + p), ex);
			}
		}

		apply(map);
	}

	protected abstract void apply(Map<ResourceLocation, T> map);
}
