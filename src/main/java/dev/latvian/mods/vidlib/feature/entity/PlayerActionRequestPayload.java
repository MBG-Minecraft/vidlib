package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.world.InteractionHand;

public record PlayerActionRequestPayload(PlayerActionType action, InteractionHand hand) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<PlayerActionRequestPayload> TYPE = VidLibPacketType.internal("player_action_request", CompositeStreamCodec.of(
		PlayerActionType.STREAM_CODEC, PlayerActionRequestPayload::action,
		DataTypes.HAND.streamCodec(), PlayerActionRequestPayload::hand,
		PlayerActionRequestPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		PlayerActionHandler.handle(ctx.player(), action, true);
	}
}
