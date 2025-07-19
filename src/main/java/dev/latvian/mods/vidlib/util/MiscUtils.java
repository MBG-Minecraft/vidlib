package dev.latvian.mods.vidlib.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SequencedCollection;

public interface MiscUtils {
	List<Comparison> COMPARISONS = List.of(Comparison.values());

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

	Lazy<Biome> VOID_BIOME = Lazy.of(() -> {
		try {
			var file = ModList.get().getModFileById(VidLib.ID).getFile().findResource("data", VidLib.ID, "worldgen", "biome", "void.json");
			try (var reader = Files.newBufferedReader(file)) {
				var json = JsonUtils.read(reader);
				return Biome.DIRECT_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	});
}
