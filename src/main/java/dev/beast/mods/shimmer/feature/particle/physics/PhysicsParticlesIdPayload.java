package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record PhysicsParticlesIdPayload(PhysicsParticlesIdData data) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<PhysicsParticlesIdPayload> TYPE = ShimmerPacketType.internal("physics_particles_id", PhysicsParticlesIdData.STREAM_CODEC.map(PhysicsParticlesIdPayload::new, PhysicsParticlesIdPayload::data));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().physicsParticles(data, ctx.remoteGameTime());
	}
}
