package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.gateway.event.HubGatewayEvent;

import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class HubGateway implements WebSocket.Listener {
	public static HubGateway client;

	public final URI uri;
	private final AtomicLong rpcId;
	private List<CharSequence> messageParts;
	private CompletableFuture<?> completedMessageFuture;
	public WebSocket webSocket;
	private Map<String, Consumer<HubGatewayEvent>> eventHandlers;

	public HubGateway(URI uri) {
		this.uri = URI.create(HubAPI.URI_BASE.resolve(uri).toString().replaceFirst("^http", "ws"));
		this.rpcId = new AtomicLong(0L);
		this.webSocket = null;
		this.messageParts = new ArrayList<>(1);
		this.completedMessageFuture = new CompletableFuture<>();
	}

	public void start() throws Exception {
		webSocket = HubAPI.HTTP_CLIENT.newWebSocketBuilder().buildAsync(uri, this).get(10L, TimeUnit.SECONDS);
		sendRPC("init", null, false);
	}

	public void stop() {
		var ws = webSocket;

		if (ws != null) {
			ws.sendClose(WebSocket.NORMAL_CLOSURE, "Closed");
			webSocket = null;
		}
	}

	private long sendRPC(String method, JsonElement params, boolean includeId) {
		var ws = webSocket;

		if (ws == null) {
			return 0L;
		}

		long id = 0L;
		var json = new JsonObject();
		json.addProperty("jsonrpc", "2.0");
		json.addProperty("method", method);

		if (params instanceof JsonObject || params instanceof JsonArray) {
			json.add("params", params);
		}

		if (includeId) {
			id = rpcId.incrementAndGet();
			json.addProperty("id", id);
		}

		ws.sendText(json.toString(), true);
		return id;
	}

	private void process(String message) {
		VidLib.LOGGER.info(message);

		try {
			var json = JsonUtils.parse(message).getAsJsonObject();
			var method = json.get("method").getAsString();
			var params = json.get("params");
			var id = json.has("id") ? json.get("id").getAsLong() : 0L;
			var event = new HubGatewayEvent(this, id, method, params);

			if (eventHandlers == null) {
				eventHandlers = new HashMap<>();
				PlatformHelper.CURRENT.collectGatewayEventHandlers(eventHandlers);
			}

			var callback = eventHandlers.get(method);

			if (callback != null) {
				callback.accept(event);
			} else {
				event.respondWithError(-32601, "Method not found");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
}
