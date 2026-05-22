package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class KVectorRegistryEvent extends SimpleRegistryTypeEvent<KVector> {
	public KVectorRegistryEvent(SimpleRegistryCollector<KVector> registry) {
		super(registry);
	}
}
