package dev.beast.mods.shimmer.feature.icon;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;

public enum EmptyIcon implements Icon {
	INSTANCE;

	public static final SimpleRegistryType.Unit<EmptyIcon> TYPE = SimpleRegistryType.unit(Shimmer.id("empty"), INSTANCE);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public IconHolder holder() {
		return IconHolder.EMPTY;
	}
}
