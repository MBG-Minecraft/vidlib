package dev.beast.mods.shimmer.feature.entity;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ForceEntityVelocityPayload(int entity, Vec3 velocity) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<ForceEntityVelocityPayload> TYPE = ShimmerPacketType.internal("force_entity_velocity", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, ForceEntityVelocityPayload::entity,
		ShimmerStreamCodecs.VEC_3, ForceEntityVelocityPayload::velocity,
		ForceEntityVelocityPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		var e = ctx.player().level().getEntity(entity);

		if (e != null) {
			e.forceSetVelocity(velocity);
		}
	}
}
