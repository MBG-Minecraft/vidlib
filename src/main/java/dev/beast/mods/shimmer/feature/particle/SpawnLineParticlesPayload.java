package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;

public record SpawnLineParticlesPayload(Map<LineParticleOptions, List<AABB>> map) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SpawnLineParticlesPayload> TYPE = ShimmerPacketType.internal("spawn_line_particles", LineParticleOptions.STREAM_CODEC.unboundedMap(ShimmerStreamCodecs.AABB.list()).map(SpawnLineParticlesPayload::new, SpawnLineParticlesPayload::map));

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
		ctx.level().lineParticles(map);
	}
}
