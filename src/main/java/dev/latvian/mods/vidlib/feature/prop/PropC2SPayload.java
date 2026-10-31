package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.codec.ByteBufCodecs;

public record PropC2SPayload(int prop, int packet, byte[] data) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<PropC2SPayload> TYPE = SimplePayloadType.vidlib("prop/c2s", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, PropC2SPayload::prop,
		ByteBufCodecs.VAR_INT, PropC2SPayload::packet,
		ByteBufCodecs.BYTE_ARRAY, PropC2SPayload::data,
		PropC2SPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = VLPlatformHelper.CURRENT.getProps(ctx.level());
		var prop = props.levelProps.get(prop());

		if (prop != null) {
			var packet = prop.type.getPacket(packet());

			if (packet != null) {
				packet.packet().handle(prop, ctx, data);
			}
		}
	}
}
