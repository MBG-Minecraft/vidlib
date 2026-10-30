package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ScreenShakeTypeRegistryEvent extends CustomRegistryTypeEvent<RegistryFriendlyByteBuf, ScreenShakeType> {
	public ScreenShakeTypeRegistryEvent(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenShakeType> registry) {
		super(registry);
	}
}
