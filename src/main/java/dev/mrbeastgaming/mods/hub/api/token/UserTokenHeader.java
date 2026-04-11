package dev.mrbeastgaming.mods.hub.api.token;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public record UserTokenHeader(UserTokenType type, int user, int session, long utc, int iteration, String name) {
	public static UserTokenHeader read(DataInput data) throws IOException {
		var type = UserTokenType.get(data.readByte() & 0xFF);
		int id = data.readInt();
		int session = data.readInt();
		long utc = data.readLong();
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

	public byte[] encode() {
		try (var bytes = new ByteArrayOutputStream(); var data = new DataOutputStream(bytes)) {
			write(data);
			return StringUtils.B64_ENCODER.encode(bytes.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String encodeToString() {
		return new String(encode(), StandardCharsets.ISO_8859_1);
	}

	@Override
	@NotNull
	public String toString() {
		return "%08X/%08X".formatted(user, session);
	}

	public void write(DataOutput data) throws IOException {
		data.writeByte(type.ordinal());
		data.writeInt(user);
		data.writeInt(session);
		data.writeLong(utc);
		IOUtils.writeVarInt(data, iteration);
		IOUtils.writeUTF(data, name);
	}
}
