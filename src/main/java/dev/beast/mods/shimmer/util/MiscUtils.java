package dev.beast.mods.shimmer.util;

import dev.beast.mods.shimmer.Shimmer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public interface MiscUtils {
	static <K, V> Map<K, V> createMap(int size, boolean ordered, boolean identity) {
		if (size == 0) {
			return Map.of();
		} else if (ordered) {
			return identity ? new Reference2ObjectLinkedOpenHashMap<>(size) : new Object2ObjectLinkedOpenHashMap<>(size);
		} else {
			return identity ? new Reference2ObjectOpenHashMap<>(size) : new Object2ObjectOpenHashMap<>(size);
		}
	}

	static Path createDir(Path path) {
		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Failed to create directory " + path.toAbsolutePath());
				throw new RuntimeException(ex);
			}
		}

		return path;
	}
}
