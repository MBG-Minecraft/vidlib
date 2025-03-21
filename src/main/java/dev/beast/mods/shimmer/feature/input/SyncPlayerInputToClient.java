package dev.beast.mods.shimmer.feature.input;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

import java.util.UUID;

public record SyncPlayerInputToClient(UUID player, PlayerInput input) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncPlayerInputToClient> TYPE = ShimmerPacketType.internal("sync_input_to_client", CompositeStreamCodec.of(
		ShimmerStreamCodecs.UUID, SyncPlayerInputToClient::player,
		PlayerInput.STREAM_CODEC, SyncPlayerInputToClient::input,
		SyncPlayerInputToClient::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		return false;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateInput(player, input);
	}
}
