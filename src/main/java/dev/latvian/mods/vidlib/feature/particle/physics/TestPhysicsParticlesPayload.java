package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record TestPhysicsParticlesPayload(PhysicsParticlesDisplayData data) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<TestPhysicsParticlesPayload> TYPE = VidLibPacketType.internal("physics_particles/test", PhysicsParticlesDisplayData.STREAM_CODEC.map(TestPhysicsParticlesPayload::new, TestPhysicsParticlesPayload::data));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			ctx.level().physicsParticles(data, ctx.level().getGameTime());
		}
	}
}
