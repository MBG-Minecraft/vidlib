package dev.latvian.mods.vidlib.util;

import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public record StringIoSupplier(String data) implements IoSupplier<InputStream> {
	@Override
	public InputStream get() {
		return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	@Override
	public @NotNull String toString() {
		return data;
	}
}
