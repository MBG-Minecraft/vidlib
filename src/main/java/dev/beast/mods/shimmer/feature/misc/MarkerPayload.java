package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record MarkerPayload(MarkerData data) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<MarkerPayload> TYPE = ShimmerPacketType.internal("marker", MarkerData.STREAM_CODEC.map(MarkerPayload::new, MarkerPayload::data));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().marker(data);
	}
}
