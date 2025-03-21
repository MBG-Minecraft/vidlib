package dev.beast.mods.shimmer.feature.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShimmerPacketPayloadContainer(ShimmerPacketPayload wrapped, long uid, long remoteGameTime) implements CustomPacketPayload {
	@Override
	public Type<ShimmerPacketPayloadContainer> type() {
		return wrapped.getType().type();
	}
}
