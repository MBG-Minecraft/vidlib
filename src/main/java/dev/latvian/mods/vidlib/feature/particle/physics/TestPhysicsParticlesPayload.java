package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.List;

public record TestPhysicsParticlesPayload(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<TestPhysicsParticlesPayload> TYPE = VidLibPacketType.internal("physics_particles/test", CompositeStreamCodec.of(
		PhysicsParticleData.STREAM_CODEC, TestPhysicsParticlesPayload::data,
		ByteBufCodecs.LONG, TestPhysicsParticlesPayload::seed,
		PositionedBlock.LIST_STREAM_CODEC, TestPhysicsParticlesPayload::blocks,
		TestPhysicsParticlesPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			ctx.level().physicsParticles(data, ctx.level().getGameTime(), seed, blocks);
		}
	}
}
