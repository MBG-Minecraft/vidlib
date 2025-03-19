package dev.beast.mods.shimmer.feature.entity;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SEntityEventPayload(int entity, String event, CompoundTag data) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<C2SEntityEventPayload> TYPE = ShimmerPacketType.internal("c2s_entity_event", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, C2SEntityEventPayload::entity,
		ByteBufCodecs.STRING_UTF8, C2SEntityEventPayload::event,
		ShimmerStreamCodecs.COMPOUND_TAG, C2SEntityEventPayload::data,
		C2SEntityEventPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		var e = ctx.player().level().getEntity(entity);

		if (e != null && ctx.player() instanceof ServerPlayer player) {
			e.onC2SEvent(event, data, player);
		}
	}
}
