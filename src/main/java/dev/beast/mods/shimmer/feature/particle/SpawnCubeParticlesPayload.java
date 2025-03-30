package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

public record SpawnCubeParticlesPayload(Map<CubeParticleOptions, List<BlockPos>> map) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SpawnCubeParticlesPayload> TYPE = ShimmerPacketType.internal("spawn_cube_particles", CubeParticleOptions.STREAM_CODEC.unboundedMap(BlockPos.STREAM_CODEC.list()).map(SpawnCubeParticlesPayload::new, SpawnCubeParticlesPayload::map));

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
		ctx.level().cubeParticles(map);
	}
}
