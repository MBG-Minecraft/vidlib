package dev.beast.mods.shimmer.feature.entity;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CEntityEventPayload(int entity, String event, CompoundTag data, long gameTime) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<S2CEntityEventPayload> TYPE = ShimmerPacketType.internal("s2c_entity_event", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, S2CEntityEventPayload::entity,
		ByteBufCodecs.STRING_UTF8, S2CEntityEventPayload::event,
		ShimmerStreamCodecs.COMPOUND_TAG, S2CEntityEventPayload::data,
		ByteBufCodecs.VAR_LONG, S2CEntityEventPayload::gameTime,
		S2CEntityEventPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		var e = ctx.player().level().getEntity(entity);

		if (e != null) {
			e.onS2CEvent(event, data);
		}
	}
}
