package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RefreshNamePayload(UUID player) implements ShimmerPacketPayload {
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
		}
	}
}
