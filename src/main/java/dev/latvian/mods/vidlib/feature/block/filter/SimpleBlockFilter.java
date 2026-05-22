package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public abstract class SimpleBlockFilter implements BlockFilter {
	private final SimpleRegistryType<BlockFilter> type;

	public SimpleBlockFilter(SimpleRegistryType<BlockFilter> type) {
		this.type = type;
	}

	@Override
	public SimpleRegistryType<?> type() {
		return type;
	}

	@Override
	public String toString() {
		return type.id();
	}
}
