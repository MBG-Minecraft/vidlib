package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;

public record TestPhysicsParticlesPayload(PhysicsParticlesDisplayData data) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<TestPhysicsParticlesPayload> TYPE = SimplePayloadType.vidlib("physics_particles/test", PhysicsParticlesDisplayData.STREAM_CODEC.map(TestPhysicsParticlesPayload::new, TestPhysicsParticlesPayload::data));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			ctx.features().physicsParticles(data, ctx.level().getGameTime());
		}
	}
}
