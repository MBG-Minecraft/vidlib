package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import io.netty.buffer.ByteBuf;

public class BlockFilterTypeRegistryEvent extends CustomRegistryTypeEvent<ByteBuf, BlockFilter> {
	public BlockFilterTypeRegistryEvent(CustomRegistryTypeCollector<ByteBuf, BlockFilter> registry) {
		super(registry);
	}
}
