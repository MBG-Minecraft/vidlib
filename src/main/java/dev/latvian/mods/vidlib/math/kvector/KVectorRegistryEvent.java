package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class KVectorRegistryEvent extends CustomRegistryTypeEvent<KVector> {
	public KVectorRegistryEvent(CustomRegistryTypeCollector<KVector> registry) {
		super(registry);
	}
}
