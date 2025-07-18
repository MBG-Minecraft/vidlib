package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record PropS2CPayload(int prop, int packet, byte[] data) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<PropS2CPayload> TYPE = VidLibPacketType.internal("prop/s2c", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, PropS2CPayload::prop,
		ByteBufCodecs.VAR_INT, PropS2CPayload::packet,
		ByteBufCodecs.BYTE_ARRAY, PropS2CPayload::data,
		PropS2CPayload::new
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
