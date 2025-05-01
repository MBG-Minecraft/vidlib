package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record PlayerActionRequestPayload(PlayerActionType action) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<PlayerActionRequestPayload> TYPE = VidLibPacketType.internal("player_action_request", PlayerActionType.STREAM_CODEC.map(PlayerActionRequestPayload::new, PlayerActionRequestPayload::action));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		PlayerActionHandler.handle(ctx.player(), action, true);
	}
}
