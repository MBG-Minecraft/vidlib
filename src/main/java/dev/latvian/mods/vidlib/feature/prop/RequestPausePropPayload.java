package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record RequestPausePropPayload(PropListType type, int id, boolean paused) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<RequestPausePropPayload> TYPE = VidLibPacketType.internal("prop/request_pause", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, RequestPausePropPayload::type,
		ByteBufCodecs.VAR_INT, RequestPausePropPayload::id,
		ByteBufCodecs.BOOL, RequestPausePropPayload::paused,
		RequestPausePropPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			var prop = ctx.level().getProps().propLists.get(type).get(id);

			if (prop != null) {
				prop.paused = paused;
				ctx.level().s2c(new PausePropPayload(type, id, paused));
			}
		}
	}
}
