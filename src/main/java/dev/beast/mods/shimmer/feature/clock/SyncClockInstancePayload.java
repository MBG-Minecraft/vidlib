package dev.beast.mods.shimmer.feature.clock;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncClockInstancePayload(ResourceLocation id, int tick, boolean ticking) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<SyncClockInstancePayload> TYPE = ShimmerPacketType.internal("sync_clock_instance", CompositeStreamCodec.of(
		ResourceLocation.STREAM_CODEC,
		SyncClockInstancePayload::id,
		ByteBufCodecs.VAR_INT,
		SyncClockInstancePayload::tick,
		ByteBufCodecs.BOOL,
		SyncClockInstancePayload::ticking,
		SyncClockInstancePayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateClockInstance(this);
	}
}
