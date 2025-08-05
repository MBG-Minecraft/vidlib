package dev.latvian.mods.vidlib.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.SequencedCollection;

public interface MiscUtils {
	Comparator<GameProfile> PROFILE_COMPARATOR = (a, b) -> a.getName().compareToIgnoreCase(b.getName());

	HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.executor(Util.backgroundExecutor())
		.followRedirects(HttpClient.Redirect.ALWAYS)
		.connectTimeout(Duration.ofSeconds(10L))
		.build();

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

	static DataResult<byte[]> fetch(String url) {
		var request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.GET()
			.timeout(Duration.ofSeconds(10L))
			.header("Accept-Language", "en-US,en;q=0.5")
			.header("User-Agent", "VidLib/1.0")
			.build();

		HttpResponse<byte[]> response = null;

		try {
			response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
			return DataResult.success(response.body());
		} catch (Exception ex) {
			if (response != null) {
				var res = response;
				return DataResult.error(() -> "Error " + res.statusCode() + ": " + ex);
			} else {
				return DataResult.error(ex::toString);
			}
		}
	}

	static GameProfile fetchProfile(String name) {
		return fetch("https://api.mojang.com/users/profiles/minecraft/" + name).flatMap(bytes -> {
			try {
				var json = JsonUtils.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8), JsonObject.class);
				return ExtraCodecs.GAME_PROFILE.parse(JsonOps.INSTANCE, json);
			} catch (Exception e) {
				return DataResult.error(() -> "Failed to parse profile json: " + e.getMessage());
			}
		}).getOrThrow();
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
