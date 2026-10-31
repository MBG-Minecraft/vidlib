package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeEvent;
import io.netty.buffer.ByteBuf;

public class TTFFileRegistryEvent extends CustomRegistryTypeEvent<ByteBuf, TTFFile> {
	public TTFFileRegistryEvent(CustomRegistryTypeCollector<ByteBuf, TTFFile> callback) {
		super(callback);
	}
}
