package dev.beast.mods.shimmer.content.clock;

import dev.beast.mods.shimmer.ShimmerNet;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClockPayload(int ticks, int delta) implements CustomPacketPayload {
	public static final Type<ClockPayload> TYPE = ShimmerNet.type("clock");

	public static final StreamCodec<ByteBuf, ClockPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ClockPayload::ticks,
		ByteBufCodecs.VAR_INT,
		ClockPayload::delta,
		ClockPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ClockBlockEntity.update(ticks, delta));
	}
}
