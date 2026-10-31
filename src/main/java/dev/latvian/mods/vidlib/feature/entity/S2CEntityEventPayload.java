package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record S2CEntityEventPayload(EntityData event) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<S2CEntityEventPayload> TYPE = SimplePayloadType.vidlib("s2c_ee", EntityData.STREAM_CODEC.map(S2CEntityEventPayload::new, S2CEntityEventPayload::event));

	@Override
	public SimplePayloadType<?> getType() {
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
