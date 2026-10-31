package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import io.netty.buffer.ByteBuf;

public class ScreenShakeTypeRegistryEvent extends CustomRegistryTypeEvent<ByteBuf, ScreenShakeType> {
	public ScreenShakeTypeRegistryEvent(CustomRegistryTypeCollector<ByteBuf, ScreenShakeType> registry) {
		super(registry);
	}
}
