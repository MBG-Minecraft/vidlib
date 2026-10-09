package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.klib.util.net.NetUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubConfig;
import dev.mrbeastgaming.mods.hub.api.data.HubChecksumPath;
import dev.mrbeastgaming.mods.hub.api.data.HubMinecraftProfile;
import dev.mrbeastgaming.mods.hub.api.gateway.HubCommonGateway;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldsResponse;
import net.minecraft.util.FastBufferedInputStream;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public interface HubAPI {
	URI URI_BASE = URI.create(Optional.ofNullable(System.getenv("MBG_HUB_API_BASE")).orElse("https://hub.mrbeastmc.com"));
	Codec<URI> URI_BASE_CODEC = KLibCodecs.relativeURI(URI_BASE);
	Codec<URI> WS_URI_BASE_CODEC = KLibCodecs.webSocketURI(URI_BASE_CODEC);

	MutableObject<Supplier<HubCommonGateway<?>>> CLIENT_GATEWAY = new MutableObject<>(() -> null);

	static HubOps<JsonElement> jsonOps() {
		return new HubOps<>(JsonOps.INSTANCE);
	}

	static HubAPIResponse send(HttpRequest request, boolean responseBody) throws IOException, InterruptedException {
		return new HubAPIResponse(NetUtils.send(request, responseBody));
	}

	static void download(HttpRequest request, Path to) throws IOException, InterruptedException {
		var response = NetUtils.send(request, HttpResponse.BodyHandlers.ofInputStream());
		var encoding = response.headers().firstValue("Content-Encoding").orElse("");

		try (var in = CompressionMethod.of(encoding).in(new FastBufferedInputStream(response.body())); var out = new BufferedOutputStream(Files.newOutputStream(to))) {
			in.transferTo(out);
		}
	}

	Lazy<ExecutorService> SEQUENTIAL_EXECUTOR = Lazy.of(() -> Executors.newSingleThreadExecutor(r -> {
		var thread = new Thread(r, "MBG-Hub-API-Sequential-Thread-%08X".formatted(r.hashCode()));
		thread.setDaemon(true);
		return thread;
	}));

	Lazy<ExecutorService> WEBSOCKET_EXECUTOR = Lazy.of(() -> Executors.newSingleThreadExecutor(r -> {
		var thread = new Thread(r, "MBG-Hub-API-Websocket-Thread-%08X".formatted(r.hashCode()));
		thread.setDaemon(true);
		return thread;
	}));

	static HttpRequest.Builder request(URI uri, Auth auth) {
		var builder = NetUtils.newRequest().uri(uri);

		if (auth == Auth.EXCLUDED) {
			return builder;
		}

		var userToken = HubConfig.userToken;

		if (!userToken.isEmpty()) {
			builder.setHeader("Authorization", "Bearer " + userToken);
		} else if (auth == Auth.REQUIRED) {
			throw new NullPointerException("Hub Auth token not found");
		}

		return builder;
	}

	static HttpRequest.Builder request(String path, Auth auth) {
		return request(URI_BASE.resolve(path), auth);
	}

	static HttpRequest.BodyPublisher jsonBody(JsonElement body) {
		return HttpRequest.BodyPublishers.ofString(JsonUtils.string(body));
	}

	static <T> HttpRequest.BodyPublisher jsonBody(Codec<T> codec, T value) {
		return jsonBody(codec.encodeStart(HubAPI.jsonOps(), value).getOrThrow());
	}

	@Nullable
	static HubCommonGateway<?> getClientGateway() {
		return CLIENT_GATEWAY.getValue().get();
	}

	@Nullable
	static HubCommonGateway<?> getClientOrServerGateway() {
		var gateway = getClientGateway();
		return gateway == null ? HubServerGateway.get() : gateway;
	}

	interface CoreAPI {
		static HubFullDataResponse getFullData() throws Exception {
			return send(request("api/full-data", Auth.NOT_REQUIRED).build(), true).json(HubFullDataResponse.CODEC);
		}

		static HubUploadResponse postUpload(HubUploadRequest request) {
			try {
				var json = HubUploadRequest.CODEC.encodeStart(HubAPI.jsonOps(), request).getOrThrow();
				return send(request("api/upload", request.gatewayToken().isEmpty() ? Auth.REQUIRED : Auth.EXCLUDED).POST(jsonBody(json)).build(), true).json(HubUploadResponse.CODEC);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to request file upload", ex);
				return new HubUploadResponse(List.of(), 0L);
			}
		}

		static HttpRequest getCountries() {
			return request("/api/countries", Auth.NOT_REQUIRED).build();
		}
	}

	interface UserAPI {
		static HttpRequest postRequestToken(String token) {
			return request("api/users/request-token", Auth.EXCLUDED).setHeader("Authorization", "Bearer " + token).POST(HttpRequest.BodyPublishers.noBody()).build();
		}
	}

	interface ProjectAPI {
		static HubProjectsResponse getAll() throws Exception {
			return send(request("api/projects", Auth.NOT_REQUIRED).build(), true).json(HubProjectsResponse.CODEC);
		}

		static HubProjectFullDataResponse getFullData(Hex32 project) throws Exception {
			return send(request("api/projects/" + project + "/full-data", Auth.NOT_REQUIRED).build(), true).json(HubProjectFullDataResponse.CODEC);
		}

		static HubProjectReplaysResponse getReplays(Hex32 project) throws Exception {
			return send(request("api/projects/" + project + "/replays", Auth.REQUIRED).GET().build(), true).json(HubProjectReplaysResponse.CODEC);
		}

		static void postLog(String projectToken, HubLogRequest request) throws Exception {
			send(request("api/projects/log/" + projectToken, Auth.REQUIRED).POST(jsonBody(HubLogRequest.CODEC, request)).build(), false);
		}

		static HubProjectFileLinkResponse postLinkFiles(Hex32 project, HubProjectFileLinkRequest request) throws Exception {
			return send(request("api/projects/" + project + "/link-files", Auth.NOT_REQUIRED).POST(jsonBody(HubProjectFileLinkRequest.CODEC, request)).build(), true).json(HubProjectFileLinkResponse.CODEC);
		}
	}

	interface MinecraftAPI {
		static HubClientSessionResponse postClientSession(HubClientSessionRequest request) throws Exception {
			return send(request("api/minecraft/client-session?v=1", Auth.NOT_REQUIRED)
				.POST(jsonBody(HubClientSessionRequest.CODEC, request))
				.timeout(Duration.ofSeconds(30L))
				.build(), true
			).json(HubClientSessionResponse.CODEC);
		}

		static HubServerSessionResponse postServerSession(HubServerSessionRequest request) throws Exception {
			return send(request("api/minecraft/server-session?v=1", Auth.REQUIRED)
				.POST(jsonBody(HubServerSessionRequest.CODEC, request))
				.timeout(Duration.ofSeconds(30L))
				.build(), true
			).json(HubServerSessionResponse.CODEC);
		}

		static HubMinecraftProfile.LinkData getLink(String name) throws Exception {
			return send(request("api/minecraft/link/" + name, Auth.REQUIRED).GET().build(), true).json(HubMinecraftProfile.LinkData.CODEC);
		}

		static HubWorldsResponse getWorlds() throws Exception {
			return send(request("api/minecraft/worlds", Auth.REQUIRED).GET().build(), true).json(HubWorldsResponse.CODEC);
		}

		static boolean postWorldRequest(UUID requestId, String worldId, UUID sessionId) throws Exception {
			var json = new JsonObject();
			json.addProperty("request_id", UndashedUuid.toString(requestId));
			json.addProperty("world_id", worldId);
			json.addProperty("session_id", UndashedUuid.toString(sessionId));
			return send(request("api/minecraft/worlds/request", Auth.REQUIRED).POST(jsonBody(json)).build(), false).isOk();
		}

		static boolean postCompleteWorldRequest(String token, List<HubChecksumPath> files) throws Exception {
			var json = new JsonObject();
			json.addProperty("token", token);

			var filesArr = new JsonArray();

			for (var file : files) {
				var obj = new JsonObject();
				obj.addProperty("checksum", file.checksum().toString());
				obj.addProperty("path", file.path());
				filesArr.add(obj);
			}

			json.add("files", filesArr);

			return send(request("api/minecraft/worlds/complete-request", Auth.REQUIRED).POST(jsonBody(json)).build(), false).isOk();
		}
	}
}
