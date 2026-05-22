package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class KNumberRegistryEvent extends SimpleRegistryTypeEvent<KNumber> {
	public KNumberRegistryEvent(SimpleRegistryCollector<KNumber> registry) {
		super(registry);
	}
}
