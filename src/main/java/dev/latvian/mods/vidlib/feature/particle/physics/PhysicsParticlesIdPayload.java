package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record PhysicsParticlesIdPayload(PhysicsParticlesIdData data) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<PhysicsParticlesIdPayload> TYPE = VidLibPacketType.internal("physics_particles_id", PhysicsParticlesIdData.STREAM_CODEC.map(PhysicsParticlesIdPayload::new, PhysicsParticlesIdPayload::data));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().physicsParticles(data);
	}
}
