package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.world.InteractionHand;

public record PlayerActionRequestPayload(PlayerActionType action, InteractionHand hand) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<PlayerActionRequestPayload> TYPE = SimplePayloadType.vidlib("player_action_request", CompositeStreamCodec.of(
		PlayerActionType.STREAM_CODEC, PlayerActionRequestPayload::action,
		DataTypes.HAND.streamCodec(), PlayerActionRequestPayload::hand,
		PlayerActionRequestPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		PlayerActionHandler.handle(ctx.player(), action, true);
	}
}
