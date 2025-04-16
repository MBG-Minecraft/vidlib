package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;

public record SpawnWindParticlesPayload(WindData data) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SpawnWindParticlesPayload> TYPE = VidLibPacketType.internal("spawn_wind_particles", WindData.STREAM_CODEC.map(SpawnWindParticlesPayload::new, SpawnWindParticlesPayload::data));

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
		ctx.level().windParticles(ctx.createRandom(), data);
	}
}
