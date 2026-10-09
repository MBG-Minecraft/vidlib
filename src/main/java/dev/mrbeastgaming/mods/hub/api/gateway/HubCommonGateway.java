package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.apps.tinyhttp.util.ByteBufferUtils;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubCommonSession;
import dev.mrbeastgaming.mods.hub.api.HubLogRequest;
import dev.mrbeastgaming.mods.hub.api.HubProjectsResponse;
import dev.mrbeastgaming.mods.hub.api.data.HubGatewayInfo;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubTVUpdateData;
import dev.mrbeastgaming.mods.hub.api.data.HubUsedPort;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.data.HubWorld;
import dev.mrbeastgaming.mods.hub.file.HubUploadBuilder;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class HubCommonGateway<M extends ReentrantBlockableEventLoop<?>> implements WebSocket.Listener {
	public static final int PACKET_DEBUG = 0;
	public static final int PACKET_UPLOAD_CHUNK = 1;
	public static final int PACKET_UPLOAD_END = 2;
	public static final int PACKET_WORLDS = 3;
	public static final int PACKET_SIZE = 4;
	public static final int PACKET_PROGRESS = 5;
	public static final int PACKET_PING = 6;
	public static final int PACKET_PONG = 7;
	public static final int PACKET_DISPLAY_PROGRESS_BAR = 8;
	public static final int PACKET_PROGRESS_BAR_STYLE = 9;
	public static final int PACKET_PROGRESS_BAR = 10;
	public static final int PACKET_REMOVE_PROGRESS_BAR = 11;
	public static final int PACKET_DOWNLOAD_WORLD = 12;

	public static void registerCommonBuiltIn(HubGatewayEventRegistry<?> registry) {
		registry.registerSynced("request_restart", HubCommonGateway::requestRestart);
		registry.register("user_updated", HubCommonGateway::userUpdated);
		registry.register("project_updated", HubCommonGateway::projectUpdated);
	}

	private static void requestRestart(ReentrantBlockableEventLoop<?> main, HubGatewayEvent event) {
		event.gateway().requestRestart();
	}

	private static void userUpdated(ReentrantBlockableEventLoop<?> main, HubGatewayEvent event) {
		var session = event.gateway().getHubSession();
		var user = HubUser.CODEC.parse(HubAPI.jsonOps(), event.params()).getOrThrow();
		var self = session.user;

		if (self != null && self.id().equals(user.id())) {
			session.user = user;
		}
	}

	private static void projectUpdated(ReentrantBlockableEventLoop<?> main, HubGatewayEvent event) {
		var session = event.gateway().getHubSession();
		var data = HubProject.DIRECT_CODEC.parse(HubAPI.jsonOps(), event.params()).getOrThrow();
		HubProjectsResponse.ALL.forget();

		var project = session.project;

		if (project != null && project.id().equals(data.id())) {
			session.project = data;
		}
	}

	public final M main;
	public Instant connected;
	public final HubGatewayInfo info;
	private List<CharSequence> messageParts;
	private List<ByteBuffer> binaryParts;
	private CompletableFuture<?> completedMessageFuture;
	private CompletableFuture<?> completedBinaryFuture;
	WebSocket webSocket;
	Map<String, BiConsumer<M, HubGatewayEvent>> eventHandlers;
	private long reconnect;
	public String status;
	public Ping lastPing;
	public Ping lastPong;
	public long lastSentPing;

	public HubCommonGateway(M main, HubGatewayInfo info) {
		this.main = main;
		this.connected = Instant.now();
		this.info = info;
		this.webSocket = null;
		this.messageParts = new ArrayList<>(1);
		this.binaryParts = new ArrayList<>(1);
		this.completedMessageFuture = new CompletableFuture<>();
		this.completedBinaryFuture = new CompletableFuture<>();
		this.reconnect = 0L;
		this.status = "Closed";
		this.lastPing = new Ping(connected, "None");
		this.lastPong = new Ping(connected, "None");
		this.lastSentPing = 0L;
	}

	public abstract HubCommonSession<?> getHubSession();

	public abstract void requestRestart();

	public void start() {
		boolean reconnecting = reconnect != 0L;
		reconnect = 0L;
		status = reconnecting ? "Reconnecting..." : "Connecting...";

		HubAPI.WEBSOCKET_EXECUTOR.get().execute(() -> {
			try {
				webSocket = info.buildClient(this).get(10L, TimeUnit.SECONDS);
				onConnected();
			} catch (Exception ex) {
				reconnect = System.currentTimeMillis() + 10000L;
				VidLib.LOGGER.error("Failed to " + (reconnecting ? "reconnect" : "connect") + " to Gateway, trying again in 10 seconds");
				status = "Early Error - Reconnecting...";
			}
		});
	}

	public void stop() {
		var ws = webSocket;

		if (ws != null) {
			try {
				ws.sendClose(WebSocket.NORMAL_CLOSURE, "Closed").get(5L, TimeUnit.SECONDS);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to close gateway", ex);
			}
		}

		reconnect = 0L;
		status = "Closed";
		webSocket = null;
	}

	public void tick() {
		long now = System.currentTimeMillis();

		if (reconnect != 0L && now >= reconnect && !isConnected()) {
			start();
		}

		if ((now - lastSentPing) > 20000L && isConnected()) {
			lastSentPing = now;
			sendPing();
		}
	}

	public void onConnected() {
		status = "Active";
		connected = Instant.now();
	}

	public static CompletableFuture<Void> runAsync(Runnable runnable) {
		return CompletableFuture.runAsync(runnable, HubAPI.WEBSOCKET_EXECUTOR.get());
	}

	public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, HubAPI.WEBSOCKET_EXECUTOR.get());
	}

	public CompletableFuture<Void> send(String method) {
		return send(method, null);
	}

	public CompletableFuture<Void> send(String method, @Nullable JsonElement params) {
		return runAsync(() -> {
			String result;

			if (params == null || params.isJsonNull()) {
				result = new JsonPrimitive(method).toString();
			} else {
				var json = new JsonObject();
				json.addProperty("method", method);
				json.add("params", params);
				result = json.toString();
			}

			var ws = webSocket;

			if (ws != null) {
				ws.sendText(result, true).join();
			}
		});
	}

	public CompletableFuture<Void> send(ByteBuffer buffer, boolean last) {
		return runAsync(() -> {
			var ws = webSocket;

			if (ws != null) {
				ws.sendBinary(buffer, last).join();
			}
		});
	}

	public CompletableFuture<Void> send(ByteBuffer buffer) {
		return send(buffer, true);
	}

	public CompletableFuture<Void> send(ByteOutput bytes) throws IOException {
		return send(ByteBuffer.wrap(bytes.toByteArray()));
	}

	public void collectEventHandlers(HubGatewayEventRegistry<M> registry) {
	}

	private void handle(String message) {
		try {
			var json = JsonUtils.parse(message);

			if (json.isJsonArray()) {
				for (var e : json.getAsJsonArray()) {
					handle0(e);
				}
			} else {
				handle0(json);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void handle(ByteBuffer buffer) {
		var data = ByteInput.of(buffer);

		try {
			var packetId = data.readVarInt();

			switch (packetId) {
				case PACKET_PING -> sendPong("Binary");
				case PACKET_PONG -> lastPong = new Ping(Instant.now(), "Binary");
				case PACKET_DISPLAY_PROGRESS_BAR -> handleDisplayProgressBar(buffer);
				case PACKET_PROGRESS_BAR_STYLE -> handleProgressBarStyle(buffer);
				case PACKET_PROGRESS_BAR -> handleProgressBar(buffer);
				case PACKET_REMOVE_PROGRESS_BAR -> handleRemoveProgressBar(buffer);
				case PACKET_DOWNLOAD_WORLD -> handleDownloadWorld(buffer);
				default -> VidLib.LOGGER.error("Unknown packet id " + packetId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void handleDisplayProgressBar(ByteBuffer buffer) throws Exception {
	}

	protected void handleProgressBarStyle(ByteBuffer buffer) throws Exception {
	}

	protected void handleProgressBar(ByteBuffer buffer) throws Exception {
	}

	protected void handleRemoveProgressBar(ByteBuffer buffer) throws Exception {
	}

	protected void handleDownloadWorld(ByteBuffer buffer) throws Exception {
	}

	private void handle0(JsonElement json) {
		if (json.isJsonObject()) {
			var obj = json.getAsJsonObject();
			var method = obj.get("method").getAsString();
			var params = obj.get("params");
			handle(method, params == null ? JsonNull.INSTANCE : params);
		} else if (json.isJsonPrimitive()) {
			handle(json.getAsString(), JsonNull.INSTANCE);
		}
	}

	private void handle(String method, JsonElement params) {
		if (method.equals("ping")) {
			sendPong("JSON");
			return;
		} else if (method.equals("pong")) {
			lastPong = new Ping(Instant.now(), "JSON");
			return;
		}

		if (eventHandlers == null) {
			eventHandlers = new HashMap<>();

			collectEventHandlers(new HubGatewayEventRegistry<>() {
				@Override
				public void register(String event, BiConsumer<M, HubGatewayEvent> callback) {
					eventHandlers.put(event, callback);
				}

				@Override
				public void registerSynced(String event, BiConsumer<M, HubGatewayEvent> callback) {
					register(event, (m, e) -> m.execute(() -> callback.accept(m, e)));
				}
			});

			eventHandlers = Map.copyOf(eventHandlers);
		}

		var event = new HubGatewayEvent(this, method, params);
		var callback = eventHandlers.get(method);

		if (callback != null) {
			callback.accept(main, event);
		} else {
			event.respondWithError(-32601, "Method not found");
		}
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		messageParts.add(data);
		webSocket.request(1L);

		if (!last) {
			return completedMessageFuture;
		}

		handle(String.join("", messageParts));
		completedMessageFuture.complete(null);
		var returnValue = completedMessageFuture;
		messageParts = new ArrayList<>(1);
		completedMessageFuture = new CompletableFuture<>();
		return returnValue;
	}

	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
		var buf = ByteBufferUtils.copy(data);
		buf.flip();
		binaryParts.add(buf);
		webSocket.request(1L);

		if (!last) {
			return completedBinaryFuture;
		}

		int size = 0;

		for (var part : binaryParts) {
			size += part.remaining();
		}

		var finalBuf = ByteBufferUtils.allocate(size, data.isDirect());

		for (var part : binaryParts) {
			finalBuf.put(part);
		}

		finalBuf.flip();

		handle(finalBuf);
		completedBinaryFuture.complete(null);
		var returnValue = completedBinaryFuture;
		binaryParts = new ArrayList<>(1);
		completedBinaryFuture = new CompletableFuture<>();
		return returnValue;
	}

	@Override
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
		lastPing = new Ping(Instant.now(), "WS");
		webSocket.request(1L);
		return webSocket.sendPong(message);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
		reconnect = System.currentTimeMillis() + 10000L;
		status = "Closed - Reconnecting...";
		webSocket = null;
		return null;
	}

	@Override
	public void onError(WebSocket ws, Throwable error) {
		reconnect = System.currentTimeMillis() + 10000L;
		status = "Late Error - Reconnecting...";
		webSocket = null;
	}

	public boolean isConnected() {
		return webSocket != null;
	}

	public CompletableFuture<Void> sendName(String name) {
		var json = new JsonObject();
		json.addProperty("name", name);
		return send("name", json);
	}

	public CompletableFuture<Void> sendStatus(String status) {
		var json = new JsonObject();
		json.addProperty("status", status);
		return send("status", json);
	}

	public CompletableFuture<Void> sendSize(long value) {
		try {
			var data = ByteOutput.ofByteBuilder(9);
			data.writeVarInt(PACKET_SIZE);
			data.writeVarLong(value);
			return send(data);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public CompletableFuture<Void> sendProgress(long value) {
		try {
			var data = ByteOutput.ofByteBuilder(9);
			data.writeVarInt(PACKET_PROGRESS);
			data.writeVarLong(value);
			return send(data);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public CompletableFuture<Void> sendUsedPorts(List<HubUsedPort> value) {
		var json = new JsonArray();

		for (var port : value) {
			json.add(port.toJson());
		}

		return send("used_ports", json);
	}

	public CompletableFuture<Void> log(Supplier<HubLogRequest> request) {
		var data = request.get();
		var json = HubLogRequest.CODEC.encodeStart(HubAPI.jsonOps(), data).getOrThrow().getAsJsonObject();
		return send("log", json);
	}

	public CompletableFuture<Void> log(int type, @Nullable Player player, Supplier<? extends Iterable<String>> content) {
		var time = Instant.now();

		return log(() -> {
			var p = player == null ? MiscUtils.CLIENT_PLAYER.getValue().get() : player;

			if (p != null) {
				return new HubLogRequest(
					Optional.of(time),
					type,
					String.join("\n", content.get()),
					p
				);
			}

			return new HubLogRequest(
				Optional.of(time),
				type,
				String.join("\n", content.get())
			);
		});
	}

	public CompletableFuture<Void> log(int type, @Nullable Player player, String content) {
		return log(type, player, () -> List.of(content));
	}

	public CompletableFuture<Void> log(int type, @Nullable Player player, String content, Throwable error) {
		return log(type, player, () -> {
			var list = new ArrayList<String>();
			list.add(content);

			error.printStackTrace(new PrintWriter(Writer.nullWriter()) {
				@Override
				public void println(Object x) {
					list.add(String.valueOf(x));
				}
			});

			return list;
		});
	}

	public CompletableFuture<Void> updateTV(int tv, HubTVUpdateData data) {
		var json = new JsonObject();
		json.addProperty("tv", tv);
		json.add("data", HubTVUpdateData.CODEC.encodeStart(HubAPI.jsonOps(), data).getOrThrow().getAsJsonObject());
		return send("update_tv", json);
	}

	public CompletableFuture<Void> updateTV(int tv, String text) {
		return updateTV(tv, new HubTVUpdateData.Text(text));
	}

	public CompletableFuture<Void> sendAvailableWorlds(List<HubWorldDirectory> value) {
		var data = new HashMap<String, HubWorld>();
		var uniqueIcons = new HashSet<Checksum>();

		return getHubSession().upload("Available World Icons", uploads -> {
			for (var world : value) {
				if (data.put(world.id(), world.toData()) == null) {
					if (!world.icon().isNil() && world.iconPath().isPresent() && uniqueIcons.add(world.icon())) {
						uploads.addFile(world.iconPath().get(), HubUploadBuilder::unlinked);
					}
				}
			}
		}).thenComposeAsync(ignored -> sendAvailableWorldList(List.copyOf(data.values())));
	}

	public CompletableFuture<Void> sendDebug(CompressionMethod compression, ByteBuffer bodyBuf) throws IOException {
		VidLib.LOGGER.info("===");
		VidLib.LOGGER.info("Body Buffer: " + bodyBuf);

		var compressedBuf = compression.compress(bodyBuf);

		VidLib.LOGGER.info("Compressed Buffer: " + compressedBuf);

		var meta = ByteOutput.ofByteBuilder(2);
		meta.writeVarInt(PACKET_DEBUG);
		meta.writeVarInt(compression.id);
		var metaBuf = ByteBuffer.wrap(meta.toByteArray());

		var data = ByteBuffer.allocateDirect(metaBuf.remaining() + compressedBuf.remaining());
		data.put(metaBuf);
		data.put(compressedBuf);
		data.flip();

		VidLib.LOGGER.info("Final Buffer: " + data);

		var future = send(data);

		var sb = new StringBuilder("Body Sent:");

		for (int i = 0; i < 16; i++) {
			sb.append(" %02X".formatted(bodyBuf.get(i) & 0xFF));
		}

		VidLib.LOGGER.info(sb.toString());

		var sbf = new StringBuilder("Final Sent:");

		for (int i = 0; i < data.limit(); i++) {
			sbf.append(" %02X".formatted(data.get(i) & 0xFF));
		}

		VidLib.LOGGER.info(sbf.toString());

		return future;
	}

	public CompletableFuture<Void> sendAvailableWorldList(List<HubWorld> list) {
		try {
			var data = ByteOutput.ofByteBuilder();
			data.writeVarInt(PACKET_WORLDS);
			data.writeVarInt(list.size());

			for (var value : list) {
				value.write(data);
			}

			return send(data);
		} catch (Exception ex) {
			return CompletableFuture.completedFuture(null);
		}
	}

	public void sendPing() {
		try {
			var data = ByteOutput.ofByteBuilder(1);
			data.writeVarInt(PACKET_PING);
			send(data);
		} catch (Exception ignored) {
		}
	}

	public void sendPong(String type) {
		lastPing = new Ping(Instant.now(), type);

		try {
			var data = ByteOutput.ofByteBuilder(1);
			data.writeVarInt(PACKET_PONG);
			send(data);
		} catch (Exception ignored) {
		}
	}

	public CompletableFuture<Void> sendVersion() {
		var json = new JsonObject();
		json.addProperty("version", VLPlatformHelper.CURRENT.getVersionString());
		return send("version", json);
	}
}
