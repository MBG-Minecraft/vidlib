package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.net.NetUtils;
import dev.mrbeastgaming.mods.hub.api.HubAPI;

import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public record HubGatewayInfo(
	URI url,
	String token
) {
	public static final Codec<HubGatewayInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
		HubAPI.WS_URI_BASE_CODEC.fieldOf("url").forGetter(HubGatewayInfo::url),
		Codec.STRING.optionalFieldOf("token", "").forGetter(HubGatewayInfo::token)
	).apply(i, HubGatewayInfo::new));

	public CompletableFuture<WebSocket> buildClient(WebSocket.Listener listener) {
		var builder = NetUtils.CLIENT.newWebSocketBuilder();

		if (!token.isEmpty()) {
			builder.header("X-MBG-Hub-Gateway-Token", token);
		}

		return builder.buildAsync(url, listener);
	}
}
