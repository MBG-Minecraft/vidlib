package dev.beast.mods.shimmer.util;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.util.registry.ShimmerRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class JsonRegistryReloadListener<T> extends JsonCodecReloadListener<T> {
	private final ShimmerRegistry<T> registry;

	public JsonRegistryReloadListener(String rootPath, Codec<T> codec, boolean includeId, ShimmerRegistry<T> registry) {
		super(rootPath, codec, includeId);
		this.registry = registry;
	}

	@Override
	protected void apply(Map<ResourceLocation, T> map) {
		registry.update(Map.copyOf(map));
	}
}
