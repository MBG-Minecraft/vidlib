package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.codec.ByteBufCodecs;

public record PropS2CPayload(int prop, int packet, byte[] data) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<PropS2CPayload> TYPE = SimplePayloadType.vidlib("prop/s2c", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, PropS2CPayload::prop,
		ByteBufCodecs.VAR_INT, PropS2CPayload::packet,
		ByteBufCodecs.BYTE_ARRAY, PropS2CPayload::data,
		PropS2CPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = VLPlatformHelper.INSTANCE.getProps(ctx.level());
		var prop = props.levelProps.get(prop());

		if (prop != null) {
			var packet = prop.type.getPacket(packet());

			if (packet != null) {
				packet.packet().handle(prop, ctx, data);
			}
		}
	}
}
