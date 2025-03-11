package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RemovePlayerDataPayload(UUID player) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<RemovePlayerDataPayload> TYPE = ShimmerPacketType.internal("remove_player_data", ShimmerStreamCodecs.UUID.map(RemovePlayerDataPayload::new, RemovePlayerDataPayload::player));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().removeSessionData(player);
	}
}
