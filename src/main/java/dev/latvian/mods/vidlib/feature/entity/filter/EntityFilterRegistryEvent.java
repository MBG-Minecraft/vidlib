package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class EntityFilterRegistryEvent extends CustomRegistryTypeEvent<EntityFilter> {
	public EntityFilterRegistryEvent(CustomRegistryTypeCollector<EntityFilter> registry) {
		super(registry);
	}
}
