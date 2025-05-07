package dev.latvian.mods.vidlib.feature.icon;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public enum EmptyIcon implements Icon {
	INSTANCE;

	public static final SimpleRegistryType.Unit<EmptyIcon> TYPE = SimpleRegistryType.unit("empty", INSTANCE);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public IconHolder holder() {
		return IconHolder.EMPTY;
	}
}
