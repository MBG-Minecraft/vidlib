package dev.latvian.mods.vidlib.feature.entity.number;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class EntityNumberRegistryEvent extends SimpleRegistryTypeEvent<EntityNumber> {
	public EntityNumberRegistryEvent(SimpleRegistryCollector<EntityNumber> registry) {
		super(registry);
	}
}
