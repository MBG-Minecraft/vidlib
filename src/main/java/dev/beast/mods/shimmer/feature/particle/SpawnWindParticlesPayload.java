package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record SpawnWindParticlesPayload(WindData data) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SpawnWindParticlesPayload> TYPE = ShimmerPacketType.internal("spawn_wind_particles", WindData.STREAM_CODEC.map(SpawnWindParticlesPayload::new, SpawnWindParticlesPayload::data));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		return false;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.level().spawnWindParticles(ctx.createRandom(), data);
	}
}
