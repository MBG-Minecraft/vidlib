package dev.latvian.mods.vidlib.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.UUID;

public interface PathIDGenerator<K> {
	PathIDGenerator<UUID> RANDOM_UUID = path -> UUID.randomUUID();

	PathIDGenerator<UUID> MD5 = path -> {
		var inst = MessageDigest.getInstance("MD5");
		var data = inst.digest(Files.readAllBytes(path));
		long msb = 0L;
		long lsb = 0L;

		for (int i = 0; i < 8; i++) {
			msb = (msb << 8L) | (data[i] & 0xFFL);
		}

		for (int i = 8; i < 16; i++) {
			lsb = (lsb << 8L) | (data[i] & 0xFFL);
		}

		return new UUID(msb, lsb);
	};

	K generate(Path path) throws Exception;
}
