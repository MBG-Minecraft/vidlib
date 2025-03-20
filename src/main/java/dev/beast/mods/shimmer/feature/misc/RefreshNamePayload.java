package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RefreshNamePayload(UUID player) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<RefreshNamePayload> TYPE = ShimmerPacketType.internal("refresh_name", ShimmerStreamCodecs.UUID.map(RefreshNamePayload::new, RefreshNamePayload::player));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		var p = ctx.player().level().getPlayerByUUID(player);

		if (p != null) {
			p.refreshDisplayName();
			p.shimmer$sessionData().refreshListedPlayers();
		}
	}
}
