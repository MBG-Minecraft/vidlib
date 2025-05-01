package dev.beast.mods.shimmer.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import dev.beast.mods.shimmer.Shimmer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.ExtraCodecs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
				Shimmer.LOGGER.error("Failed to create directory " + path.toAbsolutePath());
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

	static GameProfile fetchProfile(String name) throws IOException {
		var connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
		connection.setRequestMethod("GET");
		connection.setDoInput(true);
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		connection.setRequestProperty("User-Agent", "Shimmer/1.0");
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);

		if (connection.getResponseCode() == 200) {
			try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				var json = JsonUtils.GSON.fromJson(reader, JsonObject.class);
				return ExtraCodecs.GAME_PROFILE.parse(JsonOps.INSTANCE, json).getOrThrow();
			}
		}

		throw new IOException(connection.getResponseMessage());
	}
}
