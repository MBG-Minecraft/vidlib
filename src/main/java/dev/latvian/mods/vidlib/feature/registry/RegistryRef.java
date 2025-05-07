package dev.latvian.mods.vidlib.feature.registry;

import net.minecraft.resources.ResourceLocation;

public class RegistryRef<V> extends BasicRegistryRef<ResourceLocation, V> {
	RegistryRef(ResourceLocation id) {
		super(id);
	}

	public RegistryRef(ResourceLocation id, V value) {
		super(id, value);
	}
}
