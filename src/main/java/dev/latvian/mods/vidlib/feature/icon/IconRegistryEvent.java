package dev.latvian.mods.vidlib.feature.icon;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class IconRegistryEvent extends SimpleRegistryTypeEvent<Icon> {
	public IconRegistryEvent(SimpleRegistryCollector<Icon> registry) {
		super(registry);
	}
}
