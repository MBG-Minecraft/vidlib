package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class BulkLevelModificationRegistryEvent extends CustomRegistryTypeEvent<BulkLevelModification> {
	public BulkLevelModificationRegistryEvent(CustomRegistryTypeCollector<BulkLevelModification> registry) {
		super(registry);
	}
}
