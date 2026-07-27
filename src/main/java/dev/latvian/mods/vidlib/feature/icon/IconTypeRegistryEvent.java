package dev.latvian.mods.vidlib.feature.icon;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class IconTypeRegistryEvent extends CustomRegistryTypeEvent<RegistryFriendlyByteBuf, Icon> {
	public IconTypeRegistryEvent(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, Icon> registry) {
		super(registry);
	}
}
