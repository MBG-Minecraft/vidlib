package dev.beast.mods.shimmer.util.registry;

import net.minecraft.resources.ResourceLocation;

public class RegistryRef<V> extends BasicRegistryRef<ResourceLocation, V> {
	RegistryRef(ResourceLocation id) {
		super(id);
	}
}
