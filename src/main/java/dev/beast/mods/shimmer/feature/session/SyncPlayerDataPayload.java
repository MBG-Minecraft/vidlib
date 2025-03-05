package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record SyncPlayerDataPayload(UUID player, List<PlayerData> playerData) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<SyncPlayerDataPayload> TYPE = ShimmerPacketType.internal("sync_player_data", StreamCodec.composite(
		ShimmerStreamCodecs.UUID,
		SyncPlayerDataPayload::player,
		PlayerData.LIST_STREAM_CODEC,
		SyncPlayerDataPayload::playerData,
		SyncPlayerDataPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateSessionData(ctx.player().getUUID(), this);
	}
}
