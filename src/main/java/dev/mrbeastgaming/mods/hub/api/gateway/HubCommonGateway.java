package dev.mrbeastgaming.mods.hub.api.gateway;

import com.github.luben.zstd.Zstd;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import dev.latvian.apps.tinyhttp.util.ByteBufferUtils;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.IOFunction;
import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubLogRequest;
import dev.mrbeastgaming.mods.hub.api.gateway.tv.TVUpdateData;
import dev.mrbeastgaming.mods.hub.api.project.UsedPort;
import dev.mrbeastgaming.mods.hub.file.HubFileAction;
import dev.mrbeastgaming.mods.hub.file.UploadRequest;
import dev.mrbeastgaming.mods.hub.file.UploadRequestFile;
import dev.mrbeastgaming.mods.hub.file.UploadResponse;
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class HubCommonGateway<M extends ReentrantBlockableEventLoop<?>> implements WebSocket.Listener {
	public static final int PACKET_DEBUG = 0;
	public static final int PACKET_UPLOAD_CHUNK = 1;
	public static final int PACKET_UPLOAD_END = 2;
	public static final int PACKET_WORLD_ICON = 3;
	public static final int PACKET_SIZE = 4;
	public static final int PACKET_PROGRESS = 5;
	public static final int PACKET_PING = 6;
	public static final int PACKET_PONG = 7;

	public final M main;
	public Instant connected;
	public final URI gatewayURI;
	public final String gatewayToken;
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

	public HubCommonGateway(M main, URI gatewayURI, String gatewayToken) {
		this.main = main;
		this.connected = Instant.now();
		this.gatewayURI = gatewayURI;
		this.gatewayToken = gatewayToken;
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

	public void start() {
		boolean reconnecting = reconnect != 0L;
		reconnect = 0L;
		status = reconnecting ? "Reconnecting..." : "Connecting...";

		HubAPI.WEBSOCKET_EXECUTOR.get().execute(() -> {
			try {
				var builder = HubAPI.HTTP_CLIENT.newWebSocketBuilder();

				if (!gatewayToken.isEmpty()) {
					builder.header("X-MBG-Hub-Gateway-Token", gatewayToken);
				}

				webSocket = builder.buildAsync(gatewayURI, this).get(10L, TimeUnit.SECONDS);
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

	public CompletableFuture<Void> send(String method) {
		return send(method, null);
	}

	public CompletableFuture<Void> send(String method, @Nullable JsonElement params) {
		return CompletableFuture.runAsync(() -> {
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
		}, HubAPI.WEBSOCKET_EXECUTOR.get());
	}

	public CompletableFuture<Void> send(ByteBuffer buffer, boolean last) {
		return CompletableFuture.runAsync(() -> {
			var ws = webSocket;

			if (ws != null) {
				ws.sendBinary(buffer, last).join();
			}
		}, HubAPI.WEBSOCKET_EXECUTOR.get());
	}

	public CompletableFuture<Void> send(ByteBuffer buffer) {
		return send(buffer, true);
	}

	public void collectEventHandlers(HubGatewayEventRegistry<M> registry) {
	}

	private void process(String message) {
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

	private void process(ByteBuffer buffer) {
		var data = ByteInput.of(buffer);

		try {
			var packetId = data.readVarInt();

			switch (packetId) {
				case PACKET_PING -> sendPong("Binary");
				case PACKET_PONG -> lastPong = new Ping(Instant.now(), "Binary");
				default -> VidLib.LOGGER.error("Unknown packet id " + packetId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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

		process(String.join("", messageParts));
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

		process(finalBuf);
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
			var buf = ByteOutput.ofByteBuilder(9);
			buf.writeVarInt(PACKET_SIZE);
			buf.writeVarLong(value);
			return send(ByteBuffer.wrap(buf.toByteArray()));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public CompletableFuture<Void> sendProgress(long value) {
		try {
			var buf = ByteOutput.ofByteBuilder(9);
			buf.writeVarInt(PACKET_PROGRESS);
			buf.writeVarLong(value);
			return send(ByteBuffer.wrap(buf.toByteArray()));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public CompletableFuture<Void> sendUsedPorts(List<UsedPort> value) {
		var json = new JsonArray();

		for (var port : value) {
			json.add(port.toJson());
		}

		return send("used_ports", json);
	}

	public CompletableFuture<Void> log(Supplier<HubLogRequest> request) {
		var data = request.get();
		var json = HubLogRequest.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow().getAsJsonObject();
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

	public CompletableFuture<Void> updateTV(int tv, TVUpdateData data) {
		var json = new JsonObject();
		json.addProperty("tv", tv);
		json.add("data", TVUpdateData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow().getAsJsonObject());
		return send("update_tv", json);
	}

	public CompletableFuture<Void> updateTV(int tv, String text) {
		return updateTV(tv, new TVUpdateData.Text(text));
	}

	public CompletableFuture<Void> sendAvailableWorlds(List<HubWorldDirectory> value) {
		var result = new ArrayList<CompletableFuture<Void>>();

		var iconsToUpload = new ArrayList<UploadRequestFile>();
		var now = Instant.now();

		for (var world : value) {
			if (!world.icon().isNil() && world.iconBytes().isPresent()) {
				iconsToUpload.add(UploadRequestFile.virtual(world.id() + "/icon.png", world.iconBytes().get(), world.icon(), now));
			}
		}

		if (!iconsToUpload.isEmpty()) {
			// FIXME result.add(upload(iconsToUpload, p -> null));
		}

		result.add(sendAvailableWorldList(value.stream().map(HubWorldDirectory::toData).toList()));

		return result.size() == 1 ? result.getFirst() : CompletableFuture.allOf(result.toArray(new CompletableFuture[0]));
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

		var buf = ByteBuffer.allocateDirect(metaBuf.remaining() + compressedBuf.remaining());
		buf.put(metaBuf);
		buf.put(compressedBuf);
		buf.flip();

		VidLib.LOGGER.info("Final Buffer: " + buf);

		var future = send(buf);

		var sb = new StringBuilder("Body Sent:");

		for (int i = 0; i < 16; i++) {
			sb.append(" %02X".formatted(bodyBuf.get(i) & 0xFF));
		}

		VidLib.LOGGER.info(sb.toString());

		var sbf = new StringBuilder("Final Sent:");

		for (int i = 0; i < buf.limit(); i++) {
			sbf.append(" %02X".formatted(buf.get(i) & 0xFF));
		}

		VidLib.LOGGER.info(sbf.toString());

		return future;
	}

	public void uploadFile(
		String token,
		Path file,
		long offset,
		@Nullable ProgressItem progressItem
	) throws IOException {
		long size = Files.size(file);

		int maxChunkSize = (int) Math.min(size, 4_194_304); // 4 MB chunk
		long remaining = size - offset;
		int totalChunks = Mth.ceil(remaining / (float) maxChunkSize);

		if (progressItem != null) {
			progressItem.setSize(remaining);
			progressItem.setInfoText(ProgressItemNameFunction.SI_BYTE_SIZE);
			progressItem.setStarted();
		}

		VidLib.LOGGER.info("Uploading %,d bytes of %s".formatted(remaining, file.getFileName()));

		int chunkIndex = 0;

		var decompressedBuffer = ByteBuffer.allocateDirect(maxChunkSize);
		var compressedBuffer = ByteBuffer.allocateDirect((int) Zstd.compressBound(maxChunkSize));

		try (var channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			channel.position(offset);

			while (remaining > 0L) {
				decompressedBuffer.clear().limit((int) Math.min(maxChunkSize, remaining));

				while (decompressedBuffer.hasRemaining()) {
					if (channel.read(decompressedBuffer) == -1 && decompressedBuffer.hasRemaining()) {
						throw new EOFException();
					}
				}

				decompressedBuffer.flip();

				int decompressedSize = decompressedBuffer.remaining();

				if (decompressedSize == 0) {
					break;
				}

				compressedBuffer.clear();

				var result = Zstd.compressDirectByteBuffer(compressedBuffer, 0, compressedBuffer.remaining(), decompressedBuffer, 0, decompressedSize, Zstd.defaultCompressionLevel());

				if (Zstd.isError(result)) {
					throw new IOException("Error compressing buffer: " + Zstd.getErrorName(result));
				}

				compressedBuffer.rewind().limit((int) result);

				var data = ByteOutput.ofByteBuilder(16);
				data.writeVarInt(PACKET_UPLOAD_CHUNK);
				data.writeVarInt(chunkIndex);
				data.writeVarInt(totalChunks);
				data.writeUTF(token);
				data.writeVarLong(offset);
				data.writeVarInt(decompressedSize);
				data.writeVarInt(CompressionMethod.ZSTD.id);
				data.writeVarInt(compressedBuffer.remaining());
				var metadata = ByteBuffer.wrap(data.toByteArray());

				var finalBuffer = ByteBuffer.allocateDirect(metadata.remaining() + compressedBuffer.remaining());
				finalBuffer.put(metadata);
				finalBuffer.put(compressedBuffer);
				finalBuffer.flip();
				send(finalBuffer).join();

				offset += decompressedSize;
				remaining -= decompressedSize;
				chunkIndex++;

				if (progressItem != null) {
					progressItem.addProgress(decompressedSize);

					if (progressItem.queue.isCancelled()) {
						return;
					}
				}
			}

			var data = ByteOutput.ofByteBuilder(16);
			data.writeVarInt(PACKET_UPLOAD_END);
			data.writeUTF(token);
			send(ByteBuffer.wrap(data.toByteArray())).join();
		} finally {
			if (progressItem != null) {
				progressItem.setDone();
			}
		}
	}

	public CompletableFuture<UploadResponse> sendUploadRequest(List<UploadRequestFile> files) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return HubAPI.CoreAPI.postUpload(new UploadRequest(gatewayToken, files));
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to request file upload", ex);
				return new UploadResponse(List.of(), 0L);
			}
		}, HubAPI.WEBSOCKET_EXECUTOR.get());
	}

	public CompletableFuture<Void> upload(List<UploadRequestFile> files, IOFunction<String, Path> paths) {
		return sendUploadRequest(files).thenCompose(response -> {
			var list = new ArrayList<CompletableFuture<Void>>();

			// FIXME

			return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
		});
	}

	public CompletableFuture<Void> sendFinishUpload(List<HubFileAction> actions) {
		return send("finish_upload", HubFileAction.LIST_CODEC.encodeStart(JsonOps.INSTANCE, actions).getOrThrow());
	}

	public CompletableFuture<Void> sendAvailableWorldList(List<HubWorld> list) {
		return send("available_worlds", HubWorld.LIST_CODEC.encodeStart(JsonOps.INSTANCE, list).getOrThrow());
	}

	public void sendPing() {
		try {
			var data = ByteOutput.ofByteBuilder(1);
			data.writeVarInt(PACKET_PING);
			send(ByteBuffer.wrap(data.toByteArray()));
		} catch (Exception ignored) {
		}
	}

	public void sendPong(String type) {
		lastPing = new Ping(Instant.now(), type);

		try {
			var data = ByteOutput.ofByteBuilder(1);
			data.writeVarInt(PACKET_PONG);
			send(ByteBuffer.wrap(data.toByteArray()));
		} catch (Exception ignored) {
		}
	}
}
