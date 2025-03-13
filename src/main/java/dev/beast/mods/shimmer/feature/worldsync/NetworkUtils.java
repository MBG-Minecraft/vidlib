package dev.beast.mods.shimmer.feature.worldsync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class NetworkUtils {
	public static final int STOP_SYNC = 0;
	public static final int CANCEL_SYNC = 1;
	public static final int SEND_INFO = 2;
	public static final int SEND_FILE = 3;

	public static int readVarInt(DataInputStream in) throws IOException {
		int i = 0;
		int j = 0;

		byte b;
		do {
			b = in.readByte();
			i |= (b & 127) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
		} while ((b & 128) == 128);

		return i;
	}

	public static long readVarLong(DataInputStream in) throws IOException {
		long l = 0L;
		int i = 0;

		byte b;
		do {
			b = in.readByte();
			l |= (long) (b & 127) << i++ * 7;
			if (i > 10) {
				throw new RuntimeException("VarLong too big");
			}
		} while ((b & 128) == 128);

		return l;
	}

	public static UUID readUUID(DataInputStream in) throws IOException {
		return new UUID(in.readLong(), in.readLong());
	}

	public static void writeVarInt(DataOutputStream out, int value) throws IOException {
		while ((value & -128) != 0) {
			out.writeByte(value & 127 | 128);
			value >>>= 7;
		}

		out.writeByte(value);
	}

	public static void writeVarLong(DataOutputStream out, long value) throws IOException {
		while ((value & -128L) != 0L) {
			out.writeByte((int) (value & 127L) | 128);
			value >>>= 7;
		}

		out.writeByte((int) value);
	}

	public static void writeUUID(DataOutputStream out, UUID uuid) throws IOException {
		out.writeLong(uuid.getMostSignificantBits());
		out.writeLong(uuid.getLeastSignificantBits());
	}
}
