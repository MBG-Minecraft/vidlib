package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;

import java.io.IOException;

public record BestCompression(CompressionMethod compression, byte[] bytes, int length, int originalLength) {
	public static BestCompression find(byte[] bytes, int offset, int length) throws IOException {
		int originalLength = length;
		var compression = CompressionMethod.NONE;
		var zstd = CompressionMethod.ZSTD.compress(bytes, offset, length);
		var lz4 = CompressionMethod.LZ4.compress(bytes, offset, length);

		if (zstd.length < length) {
			compression = CompressionMethod.ZSTD;
			bytes = zstd;
			length = zstd.length;
		}

		if (lz4.length < length) {
			compression = CompressionMethod.LZ4;
			bytes = lz4;
			length = lz4.length;
		}

		return new BestCompression(compression, bytes, length, originalLength);
	}

	public static BestCompression find(byte[] bytes) throws IOException {
		return find(bytes, 0, bytes.length);
	}

	public static BestCompression zstd(byte[] bytes, int offset, int length) throws IOException {
		var compressed = CompressionMethod.ZSTD.compress(bytes, offset, length);
		return new BestCompression(CompressionMethod.ZSTD, compressed, compressed.length, length);
	}

	public static BestCompression zstd(byte[] bytes) throws IOException {
		return zstd(bytes, 0, bytes.length);
	}

	public static BestCompression lz4(byte[] bytes, int offset, int length) throws IOException {
		var compressed = CompressionMethod.LZ4.compress(bytes, offset, length);
		return new BestCompression(CompressionMethod.LZ4, compressed, compressed.length, length);
	}

	public static BestCompression lz4(byte[] bytes) throws IOException {
		return lz4(bytes, 0, bytes.length);
	}

	public static BestCompression read(ByteInput data) throws IOException {
		var compression = CompressionMethod.of(data.readUByte());

		if (compression == null) {
			throw new NullPointerException("Unknown compression method");
		}

		var bytes = data.readByteArray();
		var decompressed = compression.decompress(bytes);
		return new BestCompression(compression, decompressed, decompressed.length, bytes.length);
	}

	public void write(ByteOutput data) throws IOException {
		data.writeVarInt(compression.id);
		data.writeVarInt(length);
		data.writeAll(bytes, 0, length);
	}
}
