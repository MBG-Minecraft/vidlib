package dev.latvian.mods.vidlib.feature.zone.shape;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class ZoneShapeRegistryEvent extends SimpleRegistryTypeEvent<ZoneShape> {
	public ZoneShapeRegistryEvent(SimpleRegistryCollector<ZoneShape> registry) {
		super(registry);
	}
}
