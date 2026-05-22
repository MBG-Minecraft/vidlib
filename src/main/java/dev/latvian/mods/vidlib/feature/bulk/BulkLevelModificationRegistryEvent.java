package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class BulkLevelModificationRegistryEvent extends SimpleRegistryTypeEvent<BulkLevelModification> {
	public BulkLevelModificationRegistryEvent(SimpleRegistryCollector<BulkLevelModification> registry) {
		super(registry);
	}
}
