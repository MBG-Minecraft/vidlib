package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record S2CEntityEventPayload(EntityData event) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<S2CEntityEventPayload> TYPE = VidLibPacketType.internal("s2c_ee", EntityData.STREAM_CODEC.map(S2CEntityEventPayload::new, S2CEntityEventPayload::event));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var e = ctx.level().getEntity(event.entityId());

		if (e != null) {
			e.s2cReceived(event, ctx.player());
		}
	}
}
