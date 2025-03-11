package dev.beast.mods.shimmer.feature.clock;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncClocksPayload(List<ClockInstance> update) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncClocksPayload> TYPE = ShimmerPacketType.internal("sync_clocks", ClockInstance.STREAM_CODEC.list().map(SyncClocksPayload::new, SyncClocksPayload::update));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateClocks(ctx.player().level(), update);
	}
}
