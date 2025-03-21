package dev.beast.mods.shimmer.feature.input;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record SyncPlayerInputToServer(PlayerInput input) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<SyncPlayerInputToServer> TYPE = ShimmerPacketType.internal("sync_input_to_server", PlayerInput.STREAM_CODEC.map(SyncPlayerInputToServer::new, SyncPlayerInputToServer::input));

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
		ctx.player().shimmer$sessionData().input = input;
	}
}
