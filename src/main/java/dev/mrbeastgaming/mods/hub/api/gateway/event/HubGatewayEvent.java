package dev.mrbeastgaming.mods.hub.api.gateway.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGateway;

public record HubGatewayEvent(HubGateway gateway, long id, String method, JsonElement params) {
	public JsonObject paramsObject() {
		return params.getAsJsonObject();
	}

	public JsonArray paramsArray() {
		return params.getAsJsonArray();
	}

	public void respondWithError(int code, String error) {
		var json = new JsonObject();
		json.addProperty("jsonrpc", "2.0");
		var errorBlock = new JsonObject();
		errorBlock.addProperty("code", code);
		errorBlock.addProperty("error", error);
		json.add("error", errorBlock);
		json.add("id", id == 0L ? JsonNull.INSTANCE : new JsonPrimitive(id));
		gateway.webSocket.sendText(json.toString(), true);
	}

	public void respondWithError(String error) {
		respondWithError(-32000, error);
	}

	public void respond(JsonElement result) {
		var json = new JsonObject();
		json.addProperty("jsonrpc", "2.0");
		json.add("result", result);
		json.add("id", id == 0L ? JsonNull.INSTANCE : new JsonPrimitive(id));
		gateway.webSocket.sendText(json.toString(), true);
	}
}
