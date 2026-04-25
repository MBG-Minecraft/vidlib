package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.klib.util.MD5;
import dev.latvian.mods.klib.util.Tristate;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadRequestItem;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadResponseItem;
import net.minecraft.Util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface HubAPI {
	URI URI_BASE = URI.create(Optional.ofNullable(System.getenv("MBG_HUB_API_BASE")).orElse("https://hub.mrbeastmc.com"));

	HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.executor(Util.nonCriticalIoPool())
		.followRedirects(HttpClient.Redirect.ALWAYS)
		.connectTimeout(Duration.ofSeconds(30L))
		.build();

	Lazy<HttpRequest.Builder> HTTP_REQUEST_BASE = Lazy.of(() -> {
		var builder = HttpRequest.newBuilder();
		builder.header("User-Agent", "MBG-Hub-API-Minecraft-Mod/" + VidLib.VERSION);
		return builder;
	});

	Lazy<ExecutorService> SEQUENTIAL_EXECUTOR = Lazy.of(() -> Executors.newSingleThreadExecutor(r -> {
		var thread = new Thread(r, "Sequential-MBG-Hub-API-Thread-%08X".formatted(r.hashCode()));
		thread.setDaemon(true);
		return thread;
	}));

	static HttpRequest.Builder request(String path, Tristate auth) {
		var builder = HTTP_REQUEST_BASE.get().copy().uri(URI_BASE.resolve(path));

		if (auth == Tristate.FALSE) {
			return builder;
		}

		var userConfig = HubUserConfig.load();

		if (userConfig.token().isPresent()) {
			builder.header("Authorization", "Bearer " + userConfig.token().get());
		} else if (auth == Tristate.TRUE) {
			throw new NullPointerException("Hub Auth token not found");
		}

		return builder;
	}

	static JsonElement sendJsonRequest(HttpRequest request) throws Exception {
		var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

		if (response.statusCode() / 100 == 2) {
			try (var in = response.body()) {
				return JsonUtils.read(in);
			}
		}

		throw new IllegalStateException("HTTP Error " + response.statusCode());
	}

	static HttpRequest.BodyPublisher jsonBody(JsonElement body) {
		return HttpRequest.BodyPublishers.ofString(JsonUtils.string(body));
	}

	static HttpRequest apiCountries() {
		return request("/api/countries", Tristate.DEFAULT).build();
	}

	/*
	static CountryList apiCountries() throws Exception {
		var json = sendJsonRequest(request("/api/countries", false).build());
		return CountryList.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
	}
	 */

	static HubFullData apiFullData() throws Exception {
		var json = sendJsonRequest(request("/api/full-data", Tristate.DEFAULT).build());
		return HubFullData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
	}

	static HttpRequest apiUsersRequestToken(String token) {
		return request("/api/users/request-token/" + token, Tristate.FALSE).build();
	}

	static HttpRequest apiProjectFullData(Hex32 project) {
		return request("/api/projects/" + project + "/full-data", Tristate.DEFAULT).build();
	}

	static HttpRequest apiDesktopClientSession(String projectToken) {
		return request("/api/desktop/client-session/" + projectToken, Tristate.TRUE).timeout(Duration.ofSeconds(30L)).build();
	}

	static List<ProjectUploadResponseItem> apiProjectUpload(String token, List<ProjectUploadRequestItem> files) throws Exception {
		var body = new JsonObject();
		var filesJson = new JsonArray();

		for (var file : files) {
			var o = new JsonObject();

			if (!file.uniqueId().isEmpty()) {
				o.addProperty("unique_id", file.uniqueId().string());
			}

			o.addProperty("checksum", file.checksum().string());
			o.addProperty("size", file.size());
			o.addProperty("name", file.name());
			o.add("type", file.type().toJson());

			if (file.created() != null) {
				o.addProperty("created", file.created().toString());
			}

			if (file.assignedTo() != Hex32.NONE) {
				o.addProperty("assigned_to", file.assignedTo().toString());
			}

			if (file.assignedToMinecraft() != null) {
				o.addProperty("assigned_to_minecraft", file.assignedToMinecraft().toString());
			}

			filesJson.add(o);
		}

		body.add("files", filesJson);

		VidLib.LOGGER.info("> " + body);

		var response = sendJsonRequest(request("/api/projects/upload/" + token, Tristate.DEFAULT).POST(jsonBody(body)).build()).getAsJsonObject();

		VidLib.LOGGER.info("< " + response);

		var maxChunkSize = response.get("max_chunk_size").getAsInt();

		var result = new ArrayList<ProjectUploadResponseItem>();

		for (var fileJson : response.getAsJsonArray("files")) {
			var o = fileJson.getAsJsonObject();

			result.add(new ProjectUploadResponseItem(
				o.has("unique_id") ? MD5.fromString(o.get("unique_id").getAsString()) : MD5.NIL,
				MD5.fromString(o.get("checksum").getAsString()),
				o.get("url").getAsString(),
				o.get("offset").getAsLong(),
				maxChunkSize
			));
		}

		return result;
	}
}
