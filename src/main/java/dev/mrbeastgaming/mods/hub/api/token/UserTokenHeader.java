package dev.mrbeastgaming.mods.hub.api.token;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public record UserTokenHeader(UserTokenType type, int user, int session, Instant utc, int iteration, String name) {
	public static UserTokenHeader read(DataInput data) throws IOException {
		var type = UserTokenType.get(data.readByte() & 0xFF);
		int id = data.readInt();
		int session = data.readInt();
		var utc = IOUtils.readExactTime(data);
		int iteration = IOUtils.readVarInt(data);
		String name = IOUtils.readUTF(data);
		return new UserTokenHeader(type, id, session, utc, iteration, name);
	}

	public static UserTokenHeader parse(byte[] bytes) throws IOException {
		try (var data = new DataInputStream(new ByteArrayInputStream(bytes))) {
			return read(data);
		}
	}

	public static UserTokenHeader parse(String part) throws IOException {
		return parse(StringUtils.B64_DECODER.decode(part.getBytes(StandardCharsets.ISO_8859_1)));
	}

	@Override
	@NotNull
	public String toString() {
		return "%08X/%08X".formatted(user, session);
	}
}
