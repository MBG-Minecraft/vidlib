package dev.latvian.mods.vidlib.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

public interface IOUtils {
	Set<StandardOpenOption> WRITE_OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	Set<StandardOpenOption> APPEND_OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

	static void deleteRecursively(Path dir) throws IOException {
		try (var stream = Files.walk(dir)) {
			stream.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
	}

	static void clearDirectory(Path dir) throws IOException {
		try (var stream = Files.list(dir)) {
			for (var file : stream.toList()) {
				deleteRecursively(dir);
			}
		}
	}

	static int readVarInt(DataInput in) throws IOException {
		int value = 0;
		int count = 0;

		byte b0;
		do {
			b0 = in.readByte();
			value |= (b0 & 127) << count++ * 7;

			if (count > 5) {
				throw new RuntimeException("VarInt too big");
			}
		} while ((b0 & 128) == 128);

		return value;
	}

	static long readVarLong(DataInput in) throws IOException {
		long value = 0L;
		int count = 0;

		byte b0;
		do {
			b0 = in.readByte();
			value |= (long) (b0 & 127) << count++ * 7;

			if (count > 10) {
				throw new RuntimeException("VarLong too big");
			}
		} while ((b0 & 128) == 128);

		return value;
	}

	static byte[] readBytes(DataInput in) throws IOException {
		int length = readVarInt(in);
		var bytes = new byte[length];
		in.readFully(bytes);
		return bytes;
	}

	static String readUTF(DataInput in) throws IOException {
		return new String(readBytes(in), StandardCharsets.UTF_8);
	}

	static void writeVarInt(DataOutput out, int value) throws IOException {
		while ((value & -128) != 0) {
			out.writeByte(value & 127 | 128);
			value >>>= 7;
		}

		out.writeByte(value);
	}

	static void writeVarLong(DataOutput out, long value) throws IOException {
		while ((value & -128L) != 0L) {
			out.writeByte((int) (value & 127L) | 128);
			value >>>= 7;
		}

		out.writeByte((int) value);
	}

	static void writeBytes(DataOutput out, byte[] bytes) throws IOException {
		writeVarInt(out, bytes.length);
		out.write(bytes);
	}

	static void writeUTF(DataOutput out, String value) throws IOException {
		writeBytes(out, value.getBytes(StandardCharsets.UTF_8));
	}
}

