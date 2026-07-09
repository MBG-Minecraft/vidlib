package dev.latvian.mods.vidlib.feature.icon;

import dev.latvian.mods.klib.registry.CustomRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;

public enum EmptyIcon implements Icon {
	INSTANCE;

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, Icon> type() {
		return Icon.EMPTY;
	}

	@Override
	public String toString() {
		return "empty";
	}
}
