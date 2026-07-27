package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record PhysicsParticlesPayload(PhysicsParticlesDisplayData data, long spawnTime) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<PhysicsParticlesPayload> TYPE = VidLibPacketType.internal("physics_particles/data", CompositeStreamCodec.of(
		PhysicsParticlesDisplayData.STREAM_CODEC, PhysicsParticlesPayload::data,
		ByteBufCodecs.VAR_LONG, PhysicsParticlesPayload::spawnTime,
		PhysicsParticlesPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().physicsParticles(data, spawnTime);
	}
}
