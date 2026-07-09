package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryTypeEvent;

public class ScreenEffectRegistryEvent extends CustomRegistryTypeEvent<ScreenEffect> {
	public ScreenEffectRegistryEvent(CustomRegistryTypeCollector<ScreenEffect> registry) {
		super(registry);
	}
}
