package dev.mrbeastgaming.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.common.CommonPaths;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.Util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public interface API {
	String URL_BASE = Optional.ofNullable(System.getenv("MBG_HUB_API")).orElse("https://hub.mrbeastgaming.dev");
	Lazy<Path> DATA_DIRECTORY = CommonPaths.USER.<Path>map(path -> path.resolve("beast-hub"));

	HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.executor(Util.backgroundExecutor())
		.followRedirects(HttpClient.Redirect.ALWAYS)
		.connectTimeout(Duration.ofSeconds(10L))
		.build();

	HttpRequest.Builder HTTP_REQUEST_BASE = HttpRequest.newBuilder()
		.header("User-Agent", "MBG-API-Mod/" + VidLib.VERSION);

	Codec<Integer> HEX_ID_CODEC = Codec.STRING.comapFlatMap(id -> {
		if (id.length() == 8) {
			try {
				return DataResult.success(Integer.parseUnsignedInt(id, 16));
			} catch (Exception ignored) {
			}
		}

		return DataResult.error(() -> "Invalid hex ID '" + id + "'");
	}, "%08x"::formatted);

	static HttpRequest.Builder request(String path) {
		return HTTP_REQUEST_BASE.copy().uri(URI.create(URL_BASE + path));
	}
}
