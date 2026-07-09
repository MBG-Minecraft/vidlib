package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class ScreenShakeTypeRegistryEvent extends CustomRegistryTypeEvent<ScreenShakeType> {
	public ScreenShakeTypeRegistryEvent(CustomRegistryTypeCollector<ScreenShakeType> registry) {
		super(registry);
	}
}
