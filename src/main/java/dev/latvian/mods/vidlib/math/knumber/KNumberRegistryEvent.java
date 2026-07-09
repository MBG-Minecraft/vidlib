package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class KNumberRegistryEvent extends CustomRegistryTypeEvent<KNumber> {
	public KNumberRegistryEvent(CustomRegistryTypeCollector<KNumber> registry) {
		super(registry);
	}
}
