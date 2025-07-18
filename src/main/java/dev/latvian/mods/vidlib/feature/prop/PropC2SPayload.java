package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record PropC2SPayload(int prop, int packet, byte[] data) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<PropC2SPayload> TYPE = VidLibPacketType.internal("prop/c2s", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, PropC2SPayload::prop,
		ByteBufCodecs.VAR_INT, PropC2SPayload::packet,
		ByteBufCodecs.BYTE_ARRAY, PropC2SPayload::data,
		PropC2SPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var prop = ctx.level().getProps().levelProps.get(prop());

		if (prop != null) {
			var packet = prop.type.getPacket(packet());

			if (packet != null) {
				packet.packet().handle(prop, ctx, data);
			}
		}
	}
}
