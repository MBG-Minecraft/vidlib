package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubLogRequest;
import dev.mrbeastgaming.mods.hub.api.UsedPort;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HubCommonGateway<M extends ReentrantBlockableEventLoop<?>> implements WebSocket.Listener {
	public final M main;
	public final URI uri;
	private List<CharSequence> messageParts;
	private CompletableFuture<?> completedMessageFuture;
	WebSocket webSocket;
	Map<String, Consumer<HubGatewayEvent>> eventHandlers;
	private long reconnect;
	public String status;

	public HubCommonGateway(M main, URI uri) {
		this.main = main;
		this.uri = uri;
		this.webSocket = null;
		this.messageParts = new ArrayList<>(1);
		this.completedMessageFuture = new CompletableFuture<>();
		this.reconnect = 0L;
		this.status = "Closed";
	}

	public void start() {
		boolean reconnecting = reconnect != 0L;
		reconnect = 0L;
		status = reconnecting ? "Reconnecting..." : "Connecting...";

		HubAPI.WEBSOCKET_EXECUTOR.get().execute(() -> {
			try {
				webSocket = HubAPI.HTTP_CLIENT.newWebSocketBuilder().buildAsync(uri, this).get(10L, TimeUnit.SECONDS);
				status = "Active";
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
		if (reconnect != 0L && System.currentTimeMillis() >= reconnect && !isConnected()) {
			start();
		}
	}

	public void onConnected() {
	}

	public CompletableFuture<Void> send(String method) {
		return send0(method, null);
	}

	public CompletableFuture<Void> send(String method, JsonArray params) {
		return send0(method, params);
	}

	public CompletableFuture<Void> send(String method, JsonObject params) {
		return send0(method, params);
	}

	private CompletableFuture<Void> send0(String method, @Nullable JsonElement params) {
		return CompletableFuture.runAsync(() -> {
			String result;

			if (params == null) {
				result = new JsonPrimitive(method).toString();
			} else {
				var json = new JsonObject();
				json.addProperty("jsonrpc", "2.0");
				json.addProperty("method", method);

				if (params instanceof JsonObject || params instanceof JsonArray) {
					json.add("params", params);
				}

				result = json.toString();
			}

			var ws = webSocket;

			if (ws != null) {
				ws.sendText(result, true).join();
			}
		}, HubAPI.WEBSOCKET_EXECUTOR.get());
	}

	public void collectEventHandlers(HubGatewayEventRegistry<M> registry) {
	}

	private void process(String message) {
		// VidLib.LOGGER.info(message);

		if (eventHandlers == null) {
			eventHandlers = new HashMap<>();

			collectEventHandlers(new HubGatewayEventRegistry<>() {
				@Override
				public void register(String event, Consumer<HubGatewayEvent> callback) {
					eventHandlers.put(event, callback);
				}

				@Override
				public void registerSynced(String event, BiConsumer<M, HubGatewayEvent> callback) {
					register(event, e -> main.execute(() -> callback.accept(main, e)));
				}
			});

			eventHandlers = Map.copyOf(eventHandlers);
		}

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

	private void handle0(JsonElement json) {
		if (json.isJsonObject()) {
			var obj = json.getAsJsonObject();
			var method = obj.get("method").getAsString();
			var params = obj.get("params");
			var id = obj.has("id") ? obj.get("id").getAsLong() : 0L;
			handle(method, params, id);
		} else if (json.isJsonPrimitive()) {
			handle(json.getAsString(), null, 0L);
		}
	}

	private void handle(String method, JsonElement params, long id) {
		var event = new HubGatewayEvent(this, id, method, params);
		var callback = eventHandlers.get(method);

		if (callback != null) {
			callback.accept(event);
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
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
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

	public CompletableFuture<Void> sendSize(long size) {
		var json = new JsonObject();
		json.addProperty("size", size);
		return send("size", json);
	}

	public CompletableFuture<Void> sendUsedPorts(List<UsedPort> usedPorts) {
		var json = new JsonArray();

		for (var port : usedPorts) {
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
}
