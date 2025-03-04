package dev.beast.mods.shimmer.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

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
}
