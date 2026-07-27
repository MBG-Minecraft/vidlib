package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ScreenEffectRegistryEvent extends CustomRegistryTypeEvent<RegistryFriendlyByteBuf, ScreenEffect> {
	public ScreenEffectRegistryEvent(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenEffect> registry) {
		super(registry);
	}
}
