package dev.latvian.mods.vidlib.util;

import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record ByteArrayIoSupplier(byte[] data) implements IoSupplier<InputStream> {
	@Override
	public InputStream get() {
		return new ByteArrayInputStream(data);
	}
}
