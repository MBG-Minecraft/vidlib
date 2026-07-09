package dev.latvian.mods.vidlib.feature.zone.shape;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class ZoneShapeRegistryEvent extends CustomRegistryTypeEvent<ZoneShape> {
	public ZoneShapeRegistryEvent(CustomRegistryTypeCollector<ZoneShape> registry) {
		super(registry);
	}
}
