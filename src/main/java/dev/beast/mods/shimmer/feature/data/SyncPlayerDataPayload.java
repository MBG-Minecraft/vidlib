package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

import java.util.List;
import java.util.UUID;

public record SyncPlayerDataPayload(UUID player, List<DataMapValue> update) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncPlayerDataPayload> TYPE = ShimmerPacketType.internal("sync_player_data", CompositeStreamCodec.of(
		ShimmerStreamCodecs.UUID, SyncPlayerDataPayload::player,
		DataType.PLAYER.valueListStreamCodec, SyncPlayerDataPayload::update,
		SyncPlayerDataPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateSessionData(ctx.player(), player, update);
	}
}
