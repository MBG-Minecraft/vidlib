package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

import java.util.UUID;

public record RemovePlayerDataPayload(UUID player) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<RemovePlayerDataPayload> TYPE = VidLibPacketType.internal("remove_player_data", KLibStreamCodecs.UUID.map(RemovePlayerDataPayload::new, RemovePlayerDataPayload::player));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().removeSessionData(player);
	}
}
