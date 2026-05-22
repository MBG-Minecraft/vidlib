package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public record HubGatewayEvent(HubGateway gateway, long id, String method, JsonElement params) {
	public JsonObject paramsObject() {
		return params == null ? null : params.getAsJsonObject();
	}

	public JsonArray paramsArray() {
		return params == null ? null : params.getAsJsonArray();
	}

	@Nullable
	public CompletableFuture<WebSocket> respondWithError(int code, String error) {
		var ws = gateway.webSocket;

		if (ws != null) {
			var json = new JsonObject();
			json.addProperty("jsonrpc", "2.0");
			var errorBlock = new JsonObject();
			errorBlock.addProperty("code", code);
			errorBlock.addProperty("error", error);
			json.add("error", errorBlock);
			json.add("id", id == 0L ? JsonNull.INSTANCE : new JsonPrimitive(id));
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
			json.addProperty("jsonrpc", "2.0");
			json.add("result", result);
			json.add("id", id == 0L ? JsonNull.INSTANCE : new JsonPrimitive(id));
			return ws.sendText(json.toString(), true);
		}

		return null;
	}
}
