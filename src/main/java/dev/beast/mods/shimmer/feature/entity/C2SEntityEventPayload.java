package dev.beast.mods.shimmer.feature.entity;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SEntityEventPayload(EntityData event) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<C2SEntityEventPayload> TYPE = ShimmerPacketType.internal("c2s_entity_event", EntityData.STREAM_CODEC.map(C2SEntityEventPayload::new, C2SEntityEventPayload::event));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		var e = ctx.player().level().getEntity(event.entityId());

		if (e != null && ctx.player() instanceof ServerPlayer player) {
			e.c2sReceived(event, player);
		}
	}
}
