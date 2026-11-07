package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

import java.util.UUID;

public record RemovePlayerDataPayload(UUID player) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<RemovePlayerDataPayload> TYPE = SimplePayloadType.vidlib("remove_player_data", KLibStreamCodecs.UUID.map(RemovePlayerDataPayload::new, RemovePlayerDataPayload::player));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.worldSessionData().removePlayer(player);
	}
}
