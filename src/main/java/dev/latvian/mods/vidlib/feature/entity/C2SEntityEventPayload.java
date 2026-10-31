package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.server.level.ServerPlayer;

public record C2SEntityEventPayload(EntityData event) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<C2SEntityEventPayload> TYPE = SimplePayloadType.vidlib("c2s_ee", EntityData.STREAM_CODEC.map(C2SEntityEventPayload::new, C2SEntityEventPayload::event));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var e = ctx.level().getEntity(event.entityId());

		if (e != null && ctx.player() instanceof ServerPlayer player) {
			e.c2sReceived(event, player);
		}
	}
}
