package dev.latvian.mods.userapi;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.MiscUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * To be moved to its own mod
 */
public class API implements WebSocket.Listener {
	private static final Path GAME_CONFIG_FILE = FMLPaths.GAMEDIR.get().resolve("user-api.json");
	private static final Path LOCAL_CONFIG_DIR = Path.of(System.getProperty("user.home") + "/.latvian.dev/user-api");

	private static API instance;

	public static API get() {
		if (Files.exists(GAME_CONFIG_FILE)) {
			try (var reader = Files.newBufferedReader(GAME_CONFIG_FILE)) {
				var apiConfigJson = JsonUtils.read(reader).getAsJsonObject();

				var apiId = apiConfigJson.get("id").getAsString();
				var apiUrl = apiConfigJson.get("url").getAsString();

				while (apiUrl.endsWith("/")) {
					apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
				}

				var api = new API(apiId, apiUrl);
				api.init();
				instance = api;
			} catch (Exception ex) {
				throw new RuntimeException("Failed to parse json config", ex);
			}
		} else {
			instance = new API("", "");
		}

		return instance;
	}

	public final String apiId;
	public final String apiUrl;
	public final User unknownUser;
	private final CountDownLatch latch;
	private final Int2ObjectMap<User> cachedUsers;
	public final Codec<User> userCodec;

	private String token;
	private User self;
	private WebSocket webSocket;
	private String sessionId;

	private API(String apiId, String apiUrl) {
		this.apiId = apiId;
		this.apiUrl = apiUrl;
		this.unknownUser = new User(this, 0, "Unknown", Color.RED, Set.of());
		this.latch = new CountDownLatch(1);
		this.cachedUsers = new Int2ObjectOpenHashMap<>();

		this.userCodec = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("id").forGetter(User::id),
			Codec.STRING.fieldOf("name").forGetter(User::name),
			Color.CODEC.fieldOf("color").forGetter(User::color),
			KLibCodecs.setOf(Codec.STRING).fieldOf("roles").forGetter(User::roles)
		).apply(instance, (id, name, color, roles) -> id == 0 ? unknownUser : new User(this, id, name, color, roles)));

		this.token = "";
		this.self = unknownUser;
		this.webSocket = null;
		this.sessionId = "";
	}

	private void init() throws Exception {
		token = "";
		self = unknownUser;

		var file = LOCAL_CONFIG_DIR.resolve(apiId + ".json");
		var config = new JsonObject();
		boolean writeConfig = false;

		if (Files.exists(file)) {
			try (var reader1 = Files.newBufferedReader(file)) {
				var json1 = JsonUtils.read(reader1).getAsJsonObject();

				if (json1.has(apiId)) {
					config = json1.get(apiId).getAsJsonObject();
				}
			}
		} else {
			writeConfig = true;
		}

		token = config.has("token") ? config.get("token").getAsString() : "";

		if (config.has("self")) {
			self = userCodec.parse(JsonOps.INSTANCE, config.get("self")).getOrThrow();
		} else {
			self = unknownUser;
		}

		try {
			var req = MiscUtils.HTTP_CLIENT.send(createRequest("/init").GET().build(), HttpResponse.BodyHandlers.ofString());
			int code = req.statusCode();

			if (code / 100 == 2) {
				var json = JsonUtils.GSON.fromJson(req.body(), JsonObject.class);
				var selfUpdate = userCodec.parse(JsonOps.INSTANCE, json.get("self")).getOrThrow();

				if (!self.equals(selfUpdate)) {
					self = selfUpdate;
					writeConfig = true;
				}

				if (json.has("connect")) {
					sessionId = json.get("session_id").getAsString();
					webSocket = MiscUtils.HTTP_CLIENT
						.newWebSocketBuilder()
						.connectTimeout(Duration.ofSeconds(10L))
						.header("Authorization", "Bearer " + token)
						.header("Accept-Language", "en-US,en;q=0.5")
						.header("User-Agent", "VidLib/" + VidLib.VERSION)
						.buildAsync(URI.create(json.get("connect").getAsString()), this)
						.join();

					sendWebSocket("init", null);
					latch.await();
				}
			} else {
				if (code == 401) {
					self = unknownUser;
					token = "";
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (writeConfig) {
			config.addProperty("url", apiUrl);
			config.addProperty("token", token);
			config.add("self", userCodec.encodeStart(JsonOps.INSTANCE, self).getOrThrow());

			if (Files.notExists(LOCAL_CONFIG_DIR)) {
				Files.createDirectories(LOCAL_CONFIG_DIR);
			}

			Files.writeString(file, config.toString());
		}
	}

	public void sendWebSocket(String request, Consumer<JsonObject> callback) {
		if (webSocket != null) {
			var json = new JsonObject();
			json.addProperty("request", request);

			if (callback != null) {
				callback.accept(json);
			}

			webSocket.sendText(json.toString(), true);
		}
	}

	private HttpRequest.Builder createRequest(String path) {
		var req = MiscUtils.newRequest(apiUrl + path);

		if (!token.isEmpty()) {
			req.header("Authorization", "Bearer " + token);
		}

		return req;
	}

	public User getSelf() {
		return self;
	}

	public User getUser(int id) {
		if (id <= 0) {
			return unknownUser;
		} else if (id == self.id()) {
			return self;
		}

		synchronized (cachedUsers) {
			var user = cachedUsers.get(id);

			if (user == null) {
				cachedUsers.put(id, unknownUser);

				Thread.startVirtualThread(() -> {
					try {
						var req = MiscUtils.HTTP_CLIENT.send(createRequest("/users/%06X".formatted(id)).GET().build(), HttpResponse.BodyHandlers.ofString());

						if (req.statusCode() / 100 == 2) {
							var json = JsonUtils.GSON.fromJson(req.body(), JsonObject.class);
							var data = userCodec.parse(JsonOps.INSTANCE, json).getOrThrow();

							synchronized (cachedUsers) {
								cachedUsers.put(id, data);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			}

			return user;
		}
	}

	public String getSessionId() {
		return sessionId;
	}

	public void disconnect() {
		instance = null;
		sessionId = "";

		if (webSocket != null) {
			sendWebSocket("disconnect", null);
			webSocket.sendClose(1000, "Disconnected");
			webSocket = null;
		}
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		VidLib.LOGGER.info("API using subprotocol " + webSocket.getSubprotocol());
		WebSocket.Listener.super.onOpen(webSocket);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		var text = data.toString();

		VidLib.LOGGER.info("API text received: " + text);
		latch.countDown();
		return WebSocket.Listener.super.onText(webSocket, data, last);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		VidLib.LOGGER.error("API socket error: " + error);
		WebSocket.Listener.super.onError(webSocket, error);
	}

	public DataResult<String> uploadFile(String filename, byte[] data) throws IOException, InterruptedException {
		if (token.isEmpty()) {
			return DataResult.error(() -> "Unauthorized");
		}

		var req = MiscUtils.HTTP_CLIENT.send(createRequest("/files")
				.POST(HttpRequest.BodyPublishers.ofByteArray(data))
				.header("Content-Length", Integer.toString(data.length))
				.header("Content-Type", "application/octet-stream")
				.header("X-Filename", filename)
				.build(),
			HttpResponse.BodyHandlers.ofString()
		);

		if (req.statusCode() / 100 == 2) {
			var json = JsonUtils.GSON.fromJson(req.body(), JsonObject.class);
			return DataResult.success(json.get("url").getAsString());
		}

		return DataResult.error(() -> "Error " + req.statusCode());
	}
}
