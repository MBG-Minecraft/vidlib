package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.data.InternalPlayerData;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record UpdateTestPhysicsParticlesPayload(PhysicsParticleData data) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<UpdateTestPhysicsParticlesPayload> TYPE = ShimmerPacketType.internal("update_test_physics_particles", PhysicsParticleData.STREAM_CODEC.map(UpdateTestPhysicsParticlesPayload::new, UpdateTestPhysicsParticlesPayload::data));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().set(InternalPlayerData.TEST_PARTICLES, data);
	}
}
