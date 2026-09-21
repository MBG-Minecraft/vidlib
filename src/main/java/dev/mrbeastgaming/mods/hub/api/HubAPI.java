package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.CountingOutputStream;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.gateway.HubCommonGateway;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldsData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectReplaysData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectsData;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadRequestItem;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadResponseItem;
import dev.mrbeastgaming.mods.hub.file.ChecksumPath;
import dev.mrbeastgaming.mods.hub.file.HubProjectFileLink;
import dev.mrbeastgaming.mods.hub.file.UploadRequest;
import dev.mrbeastgaming.mods.hub.file.UploadResponse;
import net.minecraft.Util;
import net.minecraft.util.FastBufferedInputStream;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public interface HubAPI {
	URI URI_BASE = URI.create(Optional.ofNullable(System.getenv("MBG_HUB_API_BASE")).orElse("https://hub.mrbeastmc.com"));
	Codec<URI> URI_BASE_CODEC = KLibCodecs.relativeURI(URI_BASE);
	Codec<URI> WS_URI_BASE_CODEC = KLibCodecs.webSocketURI(URI_BASE_CODEC);

	MutableObject<Supplier<HubCommonGateway<?>>> CLIENT_GATEWAY = new MutableObject<>(() -> null);

	HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.executor(Util.backgroundExecutor())
		.followRedirects(HttpClient.Redirect.ALWAYS)
		.connectTimeout(Duration.ofSeconds(30L))
		.build();

	static HubAPIResponse send(HttpRequest request, boolean responseBody) throws IOException, InterruptedException {
		if (responseBody) {
			var response = MiscUtils.sendRetrying(HTTP_CLIENT, request, HttpResponse.BodyHandlers.ofInputStream());
			var encoding = response.headers().firstValue("Content-Encoding").orElse("");

			try (var in = CompressionMethod.of(encoding).in(new FastBufferedInputStream(response.body()))) {
				return new HubAPIResponse(response, response.statusCode(), in.readAllBytes());
			}
		} else {
			var response = MiscUtils.sendRetrying(HTTP_CLIENT, request, HttpResponse.BodyHandlers.discarding());
			return new HubAPIResponse(response, response.statusCode(), HubAPIResponse.NO_DATA);
		}
	}

	static void download(HttpRequest request, Path to) throws IOException, InterruptedException {
		var response = MiscUtils.sendRetrying(HTTP_CLIENT, request, HttpResponse.BodyHandlers.ofInputStream());
		var encoding = response.headers().firstValue("Content-Encoding").orElse("");

		try (var in = CompressionMethod.of(encoding).in(new FastBufferedInputStream(response.body())); var out = new BufferedOutputStream(Files.newOutputStream(to))) {
			in.transferTo(out);
		}
	}

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

	Lazy<ExecutorService> WEBSOCKET_EXECUTOR = Lazy.of(() -> Executors.newSingleThreadExecutor(r -> {
		var thread = new Thread(r, "Websocket-MBG-Hub-API-Thread-%08X".formatted(r.hashCode()));
		thread.setDaemon(true);
		return thread;
	}));

	static HttpRequest.Builder request(String path, Auth auth) {
		var builder = HTTP_REQUEST_BASE.get().copy().uri(URI_BASE.resolve(path));
		builder.header("Accept-Encoding", "zstd, gzip, deflate, br");

		if (auth == Auth.EXCLUDED) {
			return builder;
		}

		var userConfig = HubUserConfig.load();

		if (!userConfig.token().isEmpty()) {
			builder.header("Authorization", "Bearer " + userConfig.token());
		} else if (auth == Auth.REQUIRED) {
			throw new NullPointerException("Hub Auth token not found");
		}

		return builder;
	}

	static HttpRequest.BodyPublisher jsonBody(JsonElement body) {
		return HttpRequest.BodyPublishers.ofString(JsonUtils.string(body));
	}

	static <T> HttpRequest.BodyPublisher jsonBody(Codec<T> codec, T value) {
		return jsonBody(codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow());
	}

	@Nullable
	static HubCommonGateway<?> getClientGateway() {
		return CLIENT_GATEWAY.getValue().get();
	}

	@Nullable
	static HubCommonGateway<?> getClientOrServerGateway() {
		var gateway = getClientGateway();
		return gateway == null ? HubServerGateway.instance : gateway;
	}

	interface CoreAPI {
		static HubFullData getFullData() throws Exception {
			return send(request("api/full-data", Auth.NOT_REQUIRED).build(), true).json(HubFullData.CODEC);
		}

		static CompletableFuture<UploadResponse> postUpload(UploadRequest request) {
			return CompletableFuture.supplyAsync(() -> {
				try {
					var json = UploadRequest.CODEC.encodeStart(JsonOps.INSTANCE, request).getOrThrow();
					return send(request("api/upload", Auth.EXCLUDED).POST(jsonBody(json)).build(), true).json(UploadResponse.CODEC);
				} catch (Exception ex) {
					VidLib.LOGGER.error("Failed to request file upload", ex);
					return new UploadResponse(List.of(), 0L);
				}
			}, Util.nonCriticalIoPool());
		}

		@Nullable
		static CompletableFuture<Void> postFileStorage(String token, Path path, long offset, long limit, @Nullable ProgressItem progressItem, Executor executor) {
			if (limit <= 0L) {
				return null;
			}

			var compressedSize = new CountingOutputStream();

			if (progressItem != null) {
				progressItem.setInfoText("Compressing...");
				progressItem.setSize(1L);
				progressItem.addProgress(1L);
			}

			try (var out = CompressionMethod.ZSTD.out(compressedSize); var in = Files.newInputStream(path)) {
				in.skipNBytes(offset);
				in.transferTo(out);
			} catch (Exception ex) {
				return null;
			} finally {
				if (progressItem != null) {
					progressItem.setDone();
				}
			}

			if (compressedSize.getCount() > Math.min(limit, 104857600L)) {
				return null;
			}

			return CompletableFuture.runAsync(() -> {
				try {
					byte[] compressed;

					try (var in = Files.newInputStream(path)) {
						in.skipNBytes(offset);
						compressed = CompressionMethod.ZSTD.compress(in.readAllBytes());
					} finally {
						if (progressItem != null) {
							progressItem.setDone();
						}
					}

					var response = send(request("api/file-storage", Auth.EXCLUDED)
						.header("X-MBG-Hub-File-Storage-Token", token)
						.header("X-MBG-Hub-Compression-Method", CompressionMethod.ZSTD.name)
						.header("X-MBG-Hub-Offset", Long.toUnsignedString(offset))
						.header("X-Content-Length-Hint", Long.toUnsignedString(compressed.length))
						.POST(HttpRequest.BodyPublishers.ofByteArray(compressed))
						.build(), false
					);

					if (response.code() / 100 != 2) {
						throw new IOException("HTTP Error " + response.code() + " uploading " + path);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}, executor);
		}

		static CompletableFuture<Integer> postFileStorageSweep(List<Checksum> files) {
			return CompletableFuture.supplyAsync(() -> {
				try {
					var json = new JsonArray();

					for (var file : files) {
						json.add(file.toString());
					}

					return send(request("api/file-storage/sweep", files.isEmpty() ? Auth.REQUIRED : Auth.NOT_REQUIRED)
						.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
						.build(), true
					).json().getAsJsonObject().get("removed").getAsInt();
				} catch (Exception ignored) {
					return 0;
				}
			}, Util.nonCriticalIoPool());
		}

		static HttpRequest getCountries() {
			return request("/api/countries", Auth.NOT_REQUIRED).build();
		}
	}

	interface UserAPI {
		static HttpRequest postRequestToken(String token) {
			return request("api/users/request-token", Auth.EXCLUDED).header("Authorization", "Bearer " + token).POST(HttpRequest.BodyPublishers.noBody()).build();
		}
	}

	interface ProjectAPI {
		static HubProjectsData getAll() throws Exception {
			return send(request("api/projects", Auth.NOT_REQUIRED).build(), true).json(HubProjectsData.CODEC);
		}

		static HttpRequest getFullData(Hex32 project) {
			return request("api/projects/" + project + "/full-data", Auth.NOT_REQUIRED).build();
		}

		static List<ProjectUploadResponseItem> postUpload(String projectToken, List<ProjectUploadRequestItem> files) throws Exception {
			var body = new JsonObject();
			var filesJson = new JsonArray();

			for (var file : files) {
				var o = new JsonObject();

				if (!file.uniqueId().isNil()) {
					o.addProperty("unique_id", file.uniqueId().toString());
				}

				o.addProperty("checksum", file.checksum().toString());
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

			var response = send(request("api/projects/upload/" + projectToken, Auth.NOT_REQUIRED).POST(jsonBody(body)).build(), true).json().getAsJsonObject();

			var maxChunkSize = response.get("max_chunk_size").getAsInt();

			var result = new ArrayList<ProjectUploadResponseItem>();

			for (var fileJson : response.getAsJsonArray("files")) {
				var o = fileJson.getAsJsonObject();

				result.add(new ProjectUploadResponseItem(
					o.has("unique_id") ? Checksum.of(o.get("unique_id").getAsString()) : NoChecksum.INSTANCE,
					Checksum.of(o.get("checksum").getAsString()),
					o.has("name") ? o.get("name").getAsString() : "",
					o.get("url").getAsString(),
					o.get("offset").getAsLong(),
					maxChunkSize
				));
			}

			return result;
		}

		static HubProjectReplaysData getReplays(Hex32 project) throws Exception {
			var response = send(request("api/projects/" + project + "/replays", Auth.REQUIRED).GET().build(), true).json().getAsJsonObject();
			return HubProjectReplaysData.CODEC.parse(JsonOps.INSTANCE, response).getOrThrow();
		}

		static void postLog(String projectToken, HubLogRequest request) throws Exception {
			var json = HubLogRequest.CODEC.encodeStart(JsonOps.INSTANCE, request).getOrThrow();
			HTTP_CLIENT.send(request("api/projects/log/" + projectToken, Auth.REQUIRED).POST(jsonBody(json)).build(), HttpResponse.BodyHandlers.discarding());
		}

		static void postLinkFiles(Hex32 project, List<HubProjectFileLink> files) throws Exception {
			var json = HubProjectFileLink.LIST_CODEC.encodeStart(JsonOps.INSTANCE, files).getOrThrow();
			HTTP_CLIENT.send(request("api/projects/" + project + "/link-files", Auth.NOT_REQUIRED).POST(jsonBody(json)).build(), HttpResponse.BodyHandlers.discarding());
		}
	}

	interface MinecraftAPI {
		static HubClientSessionData postClientSession(HubClientSessionDataRequest request) throws Exception {
			return send(request("api/minecraft/client-session?v=1", Auth.NOT_REQUIRED)
				.POST(jsonBody(HubClientSessionDataRequest.CODEC, request))
				.timeout(Duration.ofSeconds(30L))
				.build(), true
			).json(HubClientSessionData.CODEC);
		}

		static HubServerSessionData postServerSession(HubServerSessionDataRequest request) throws Exception {
			return send(request("api/minecraft/server-session?v=1", Auth.REQUIRED)
				.POST(jsonBody(HubServerSessionDataRequest.CODEC, request))
				.timeout(Duration.ofSeconds(30L))
				.build(), true
			).json(HubServerSessionData.CODEC);
		}

		static HubMinecraftProfileData.LinkData getLink(String name) throws Exception {
			var response = send(request("api/minecraft/link/" + name, Auth.REQUIRED).GET().build(), true).json().getAsJsonObject();

			return new HubMinecraftProfileData.LinkData(
				HubMinecraftProfileData.CODEC.parse(JsonOps.INSTANCE, response).getOrThrow(),
				response.has("token") ? response.get("token").getAsString() : ""
			);
		}

		static HubWorldsData getWorlds() throws Exception {
			return send(request("api/minecraft/worlds", Auth.REQUIRED).GET().build(), true).json(HubWorldsData.CODEC);
		}

		static boolean postWorldRequest(UUID requestId, String worldId, UUID sessionId) throws Exception {
			var json = new JsonObject();
			json.addProperty("request_id", UndashedUuid.toString(requestId));
			json.addProperty("world_id", worldId);
			json.addProperty("session_id", UndashedUuid.toString(sessionId));
			return send(request("api/minecraft/worlds/request", Auth.REQUIRED).POST(HttpRequest.BodyPublishers.ofString(json.toString())).build(), false).isOk();
		}

		static boolean postCompleteWorldRequest(String token, List<ChecksumPath> files) throws Exception {
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

			return send(request("api/minecraft/worlds/complete-request", Auth.REQUIRED).POST(HttpRequest.BodyPublishers.ofString(json.toString())).build(), false).isOk();
		}
	}
}
