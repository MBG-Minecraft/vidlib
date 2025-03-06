package dev.beast.mods.shimmer.feature.clock;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncClockFontsPayload(List<ClockFont> update) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<SyncClockFontsPayload> TYPE = ShimmerPacketType.internal("sync_clock_fonts", ClockFont.DIRECT_STREAM_CODEC.apply(ByteBufCodecs.list()).map(SyncClockFontsPayload::new, SyncClockFontsPayload::update));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateClockFonts(update);
	}
}
