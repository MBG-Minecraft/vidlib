package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.klib.registry.CustomRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;

public abstract class SimpleBlockFilter implements BlockFilter {
	private final CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> type;

	public SimpleBlockFilter(CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> type) {
		this.type = type;
	}

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, BlockFilter> type() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
