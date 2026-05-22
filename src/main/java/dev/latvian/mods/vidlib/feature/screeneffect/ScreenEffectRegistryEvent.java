package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryTypeEvent;

public class ScreenEffectRegistryEvent extends SimpleRegistryTypeEvent<ScreenEffect> {
	public ScreenEffectRegistryEvent(SimpleRegistryCollector<ScreenEffect> registry) {
		super(registry);
	}
}
