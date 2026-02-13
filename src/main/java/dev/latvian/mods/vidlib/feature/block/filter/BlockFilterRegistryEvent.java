package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class BlockFilterRegistryEvent extends SimpleRegistryTypeEvent<BlockFilter> {
	public BlockFilterRegistryEvent(SimpleRegistryCollector<BlockFilter> registry) {
		super(registry);
	}
}
