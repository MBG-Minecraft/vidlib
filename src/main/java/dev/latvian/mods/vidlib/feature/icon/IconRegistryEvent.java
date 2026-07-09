package dev.latvian.mods.vidlib.feature.icon;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class IconRegistryEvent extends CustomRegistryTypeEvent<Icon> {
	public IconRegistryEvent(CustomRegistryTypeCollector<Icon> registry) {
		super(registry);
	}
}
