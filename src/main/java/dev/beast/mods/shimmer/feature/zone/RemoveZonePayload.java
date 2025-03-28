package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

import java.util.UUID;

public record RemoveZonePayload(UUID uuid) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<RemoveZonePayload> TYPE = ShimmerPacketType.internal("remove_zone", ShimmerStreamCodecs.UUID.map(RemoveZonePayload::new, RemoveZonePayload::uuid));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().shimmer$getEnvironment().removeZone(uuid);
	}
}
