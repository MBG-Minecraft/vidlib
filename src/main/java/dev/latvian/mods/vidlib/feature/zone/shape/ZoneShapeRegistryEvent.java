package dev.latvian.mods.vidlib.feature.zone.shape;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import io.netty.buffer.ByteBuf;

public class ZoneShapeRegistryEvent extends CustomRegistryTypeEvent<ByteBuf, ZoneShape> {
	public ZoneShapeRegistryEvent(CustomRegistryTypeCollector<ByteBuf, ZoneShape> registry) {
		super(registry);
	}
}
