package dev.latvian.mods.vidlib.feature.registry;

import net.minecraft.resources.Identifier;

public class RegistryRef<V> extends BasicRegistryRef<Identifier, V> {
	RegistryRef(Identifier id) {
		super(id);
	}

	public RegistryRef(Identifier id, V value) {
		super(id, value);
	}
}
