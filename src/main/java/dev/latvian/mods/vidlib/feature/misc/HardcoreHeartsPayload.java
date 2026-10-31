package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;

public record HardcoreHeartsPayload(boolean hardcore) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<HardcoreHeartsPayload> TYPE = SimplePayloadType.vidlib("hardcore_hearts", CompositeStreamCodec.of(
		ByteBufCodecs.BOOL, HardcoreHeartsPayload::hardcore,
		HardcoreHeartsPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().setHardcoreHearts(hardcore);
	}
}
