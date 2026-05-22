package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class ScreenShakeTypeRegistryEvent extends SimpleRegistryTypeEvent<ScreenShakeType> {
	public ScreenShakeTypeRegistryEvent(SimpleRegistryCollector<ScreenShakeType> registry) {
		super(registry);
	}
}
