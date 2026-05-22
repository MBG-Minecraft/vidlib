package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class EntityFilterRegistryEvent extends SimpleRegistryTypeEvent<EntityFilter> {
	public EntityFilterRegistryEvent(SimpleRegistryCollector<EntityFilter> registry) {
		super(registry);
	}
}
