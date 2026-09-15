package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public record HubGatewayEvent(HubCommonGateway<?> gateway, String method, JsonElement params) {
	public JsonObject paramsObject() {
		return params.isJsonNull() ? null : params.getAsJsonObject();
	}

	public JsonArray paramsArray() {
		return params.isJsonNull() ? null : params.getAsJsonArray();
	}

	@Nullable
	public CompletableFuture<WebSocket> respondWithError(int code, String error) {
		var ws = gateway.webSocket;

		if (ws != null) {
			var json = new JsonObject();
			var errorBlock = new JsonObject();
			errorBlock.addProperty("code", code);
			errorBlock.addProperty("error", error);
			json.add("error", errorBlock);
			return ws.sendText(json.toString(), true);
		} else {
			return null;
		}
	}

	@Nullable
	public CompletableFuture<WebSocket> respondWithError(String error) {
		return respondWithError(-32000, error);
	}

	@Nullable
	public CompletableFuture<WebSocket> respond(JsonElement result) {
		var ws = gateway.webSocket;

		if (ws != null) {
			var json = new JsonObject();
			json.add("result", result);
			return ws.sendText(json.toString(), true);
		}

		return null;
	}
}
