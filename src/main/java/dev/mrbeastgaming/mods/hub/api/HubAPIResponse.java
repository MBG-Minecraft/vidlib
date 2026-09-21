package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.JsonUtils;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public record HubAPIResponse(HttpResponse<?> response, int code, byte[] data) {
	public static final byte[] NO_DATA = new byte[0];

	public boolean isOk() {
		return code / 100 == 2;
	}

	public String string() {
		return new String(data, StandardCharsets.UTF_8);
	}

	public JsonElement json() {
		if (code / 100 == 2) {
			return JsonUtils.parse(string());
		}

		var error = "HTTP Error " + code;

		if (code == 500 || code / 100 == 4) {
			error += ": " + string();
		}

		throw new IllegalStateException(error);
	}

	public <T> T json(Codec<T> codec) {
		return codec.parse(JsonOps.INSTANCE, json()).getOrThrow();
	}
}
