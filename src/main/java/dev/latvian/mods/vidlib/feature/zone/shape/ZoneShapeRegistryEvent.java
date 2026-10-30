package dev.latvian.mods.vidlib.feature.zone.shape;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class ZoneShapeRegistryEvent extends CustomRegistryTypeEvent<RegistryFriendlyByteBuf, ZoneShape> {
	public ZoneShapeRegistryEvent(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ZoneShape> registry) {
		super(registry);
	}
}
