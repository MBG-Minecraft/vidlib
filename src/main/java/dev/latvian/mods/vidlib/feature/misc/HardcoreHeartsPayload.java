package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record HardcoreHeartsPayload(boolean hardcore) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<HardcoreHeartsPayload> TYPE = VidLibPacketType.internal("hardcore_hearts", CompositeStreamCodec.of(
		ByteBufCodecs.BOOL, HardcoreHeartsPayload::hardcore,
		HardcoreHeartsPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().setHardcoreHearts(hardcore);
	}
}
