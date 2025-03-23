package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PhysicsParticlesIdPayload(ResourceLocation id, List<PositionedBlock> blocks, long seed) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<PhysicsParticlesIdPayload> TYPE = ShimmerPacketType.internal("physics_particles_id", CompositeStreamCodec.of(
		ShimmerStreamCodecs.VIDEO_ID, PhysicsParticlesIdPayload::id,
		PositionedBlock.LIST_STREAM_CODEC, PhysicsParticlesIdPayload::blocks,
		ByteBufCodecs.LONG, PhysicsParticlesIdPayload::seed,
		PhysicsParticlesIdPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().physicsParticles(id, blocks, seed);
	}
}
