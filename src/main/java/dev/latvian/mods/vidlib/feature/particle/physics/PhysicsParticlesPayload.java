package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;

public record PhysicsParticlesPayload(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<PhysicsParticlesPayload> TYPE = VidLibPacketType.internal("physics_particles", CompositeStreamCodec.of(
		PhysicsParticleData.STREAM_CODEC, PhysicsParticlesPayload::data,
		ByteBufCodecs.LONG, PhysicsParticlesPayload::seed,
		PositionedBlock.LIST_STREAM_CODEC, PhysicsParticlesPayload::blocks,
		PhysicsParticlesPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().physicsParticles(data, ctx.remoteGameTime(), seed, blocks);
	}
}
