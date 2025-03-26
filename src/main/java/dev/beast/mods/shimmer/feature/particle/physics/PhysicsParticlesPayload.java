package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;

public record PhysicsParticlesPayload(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<PhysicsParticlesPayload> TYPE = ShimmerPacketType.internal("physics_particles", CompositeStreamCodec.of(
		PhysicsParticleData.STREAM_CODEC, PhysicsParticlesPayload::data,
		ByteBufCodecs.LONG, PhysicsParticlesPayload::seed,
		PositionedBlock.LIST_STREAM_CODEC, PhysicsParticlesPayload::blocks,
		PhysicsParticlesPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().physicsParticles(data, seed, blocks);
	}
}
