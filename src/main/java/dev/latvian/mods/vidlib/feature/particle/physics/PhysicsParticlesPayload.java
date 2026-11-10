package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;

public record PhysicsParticlesPayload(PhysicsParticlesDisplayData data, long spawnTime) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<PhysicsParticlesPayload> TYPE = SimplePayloadType.vidlib("physics_particles/data", CompositeStreamCodec.of(
		PhysicsParticlesDisplayData.STREAM_CODEC, PhysicsParticlesPayload::data,
		ByteBufCodecs.VAR_LONG, PhysicsParticlesPayload::spawnTime,
		PhysicsParticlesPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.features().physicsParticles(data, spawnTime);
	}
}
