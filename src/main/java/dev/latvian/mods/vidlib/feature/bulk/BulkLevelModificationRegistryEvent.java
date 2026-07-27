package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import io.netty.buffer.ByteBuf;

public class BulkLevelModificationRegistryEvent extends CustomRegistryTypeEvent<ByteBuf, BulkLevelModification> {
	public BulkLevelModificationRegistryEvent(CustomRegistryTypeCollector<ByteBuf, BulkLevelModification> registry) {
		super(registry);
	}
}
