package dev.latvian.mods.vidlib.util;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.SequencedCollection;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public interface MiscUtils {
	Runnable NO_OP = () -> {
	};

	Comparator<GameProfile> PROFILE_COMPARATOR = (a, b) -> a.getName().compareToIgnoreCase(b.getName());

	HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.executor(Util.backgroundExecutor())
		.followRedirects(HttpClient.Redirect.ALWAYS)
		.connectTimeout(Duration.ofSeconds(10L))
		.build();

	RegistryAccess STATIC_REGISTRY_ACCESS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

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

	static HttpRequest.Builder newRequest(String url) {
		return HttpRequest.newBuilder()
			.uri(URI.create(url))
			.timeout(Duration.ofSeconds(10L))
			.header("Accept-Language", "en-US,en;q=0.5")
			.header("User-Agent", "VidLib/" + VidLib.VERSION);
	}

	static DataResult<byte[]> fetch(String url) {
		var request = newRequest(url).GET().build();
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

	static <T> T[] fastIndexedLookup(T[] values, ToIntFunction<T> idGetter, IntFunction<T[]> arrayConstructor) {
		int max = 0;

		for (var type : values) {
			max = Math.max(max, idGetter.applyAsInt(type));
		}

		var lookup = arrayConstructor.apply(max + 1);

		for (var type : values) {
			lookup[idGetter.applyAsInt(type)] = type;
		}

		return lookup;
	}
}
