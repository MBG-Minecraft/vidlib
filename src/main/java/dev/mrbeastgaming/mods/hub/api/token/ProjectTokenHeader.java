package dev.mrbeastgaming.mods.hub.api.token;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public record ProjectTokenHeader(Hex32 project, long utc, int iteration) {
	public static ProjectTokenHeader read(DataInput data) throws IOException {
		int project = data.readInt();
		long utc = data.readLong();
		int iteration = IOUtils.readVarInt(data);
		return new ProjectTokenHeader(Hex32.of(project), utc, iteration);
	}

	public static ProjectTokenHeader parse(byte[] bytes) throws IOException {
		try (var data = new DataInputStream(new ByteArrayInputStream(bytes))) {
			return read(data);
		}
	}

	public static ProjectTokenHeader parse(String part) throws IOException {
		return parse(StringUtils.B64_DECODER.decode(part.getBytes(StandardCharsets.ISO_8859_1)));
	}

	@Override
	@NotNull
	public String toString() {
		return project.toString();
	}
}
