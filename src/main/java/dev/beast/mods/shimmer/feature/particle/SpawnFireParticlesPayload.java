package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;

public record SpawnFireParticlesPayload(FireData data) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SpawnFireParticlesPayload> TYPE = ShimmerPacketType.internal("spawn_fire_particles", FireData.STREAM_CODEC.map(SpawnFireParticlesPayload::new, SpawnFireParticlesPayload::data));

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
		ctx.level().spawnFireParticles(ctx.createRandom(), data);
	}
}
