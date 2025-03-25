package dev.beast.mods.shimmer.feature.clock;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record SyncClocksPayload(Map<ResourceLocation, ClockValue> map) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncClocksPayload> TYPE = ShimmerPacketType.internal("sync_clocks", ShimmerStreamCodecs.VIDEO_ID.unboundedMap(ClockValue.STREAM_CODEC).map(SyncClocksPayload::new, SyncClocksPayload::map));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateClocks(map);
	}
}
