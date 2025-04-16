package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record SpawnFireParticlesPayload(FireData data) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SpawnFireParticlesPayload> TYPE = VidLibPacketType.internal("spawn_fire_particles", FireData.STREAM_CODEC.map(SpawnFireParticlesPayload::new, SpawnFireParticlesPayload::data));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		return false;
	}

	@Override
	public void handle(Context ctx) {
		ctx.level().fireParticles(ctx.createRandom(), data);
	}
}
