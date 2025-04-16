package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.vidlib.VidLib;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SequencedCollection;

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
				VidLib.LOGGER.error("Failed to create directory " + path.toAbsolutePath());
				throw new RuntimeException(ex);
			}
		}

		return path;
	}

	static <T> SequencedCollection<T> toSequencedCollection(Iterable<T> collection) {
		if (collection instanceof SequencedCollection) {
			return (SequencedCollection<T>) collection;
		}

		var list = new ArrayList<T>();

		for (var element : collection) {
			list.add(element);
		}

		return list;
	}

	static int size(Iterable<?> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<?>) iterable).size();
		}

		int size = 0;

		for (var ignored : iterable) {
			size++;
		}

		return size;
	}

	static boolean isEmpty(Iterable<?> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<?>) iterable).isEmpty();
		}

		return iterable.iterator().hasNext();
	}
}
