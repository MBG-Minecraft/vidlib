package dev.latvian.mods.vidlib.feature.entity.number;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class EntityNumberRegistryEvent extends CustomRegistryTypeEvent<EntityNumber> {
	public EntityNumberRegistryEvent(CustomRegistryTypeCollector<EntityNumber> registry) {
		super(registry);
	}
}
