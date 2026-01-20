package dev.latvian.mods.vidlib.util;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;

public class JsonRegistryReloadListener<T> extends JsonCodecReloadListener<T> {
	private final VLRegistry<T> registry;

	public JsonRegistryReloadListener(String rootPath, Codec<T> codec, boolean includeId, VLRegistry<T> registry) {
		super(rootPath, codec, includeId);
		this.registry = registry;
	}

	@Override
	protected void apply(ResourceManager resourceManager, Map<ResourceLocation, T> map) {
		registry.update(Map.copyOf(map));
	}
}
