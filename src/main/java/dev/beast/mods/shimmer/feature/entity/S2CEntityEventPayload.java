package dev.beast.mods.shimmer.feature.entity;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CEntityEventPayload(EntityData event) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<S2CEntityEventPayload> TYPE = ShimmerPacketType.internal("s2c_ee", EntityData.STREAM_CODEC.map(S2CEntityEventPayload::new, S2CEntityEventPayload::event));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		var e = ctx.player().level().getEntity(event.entityId());

		if (e != null) {
			e.s2cReceived(event);
		}
	}
}
