package dev.beast.mods.shimmer.feature.worldsync;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record WorldIndex(String name, long totalWorldSize, Map<String, LocalFile> files, boolean found) {
	public static WorldIndex load(Path directoryPath) throws Exception {
		var name = directoryPath.getFileName().toString();
		var indexFilePath = directoryPath.resolve("index.txt");

		if (Files.exists(indexFilePath)) {
			long totalSize = 0L;
			var localFiles = new HashMap<String, LocalFile>();

			for (var line : Files.readAllLines(indexFilePath)) {
				var parts = line.trim().split(" ", 3);

				if (parts.length == 3) {
					if (parts[0].equals("#")) {
						if (parts[1].equals("size")) {
							totalSize = Long.parseUnsignedLong(parts[2]);
						} else if (parts[1].equals("name")) {
							name = parts[2];
						}
					} else {
						var checksum = UUID.fromString(parts[0]);
						long size = Long.parseUnsignedLong(parts[1]);
						var path = parts[2];
						localFiles.put(path, new LocalFile(path, checksum, size, directoryPath.resolve(checksum.toString())));
					}
				}
			}

			return new WorldIndex(name, totalSize, localFiles, true);
		}

		return new WorldIndex(name, 0L, Collections.emptyMap(), false);
	}
}
