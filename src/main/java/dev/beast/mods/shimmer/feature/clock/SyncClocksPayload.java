package dev.beast.mods.shimmer.feature.clock;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncClocksPayload(List<ClockInstance> update) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<SyncClocksPayload> TYPE = ShimmerPacketType.internal("sync_clocks", ClockInstance.STREAM_CODEC.apply(ByteBufCodecs.list()).map(SyncClocksPayload::new, SyncClocksPayload::update));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateClocks(ctx.player().level(), this);
	}
}
