package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

public record SpawnCubeParticlesPayload(Map<CubeParticleOptions, List<BlockPos>> map) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SpawnCubeParticlesPayload> TYPE = VidLibPacketType.internal("spawn_cube_particles", CubeParticleOptions.STREAM_CODEC.unboundedMap(BlockPos.STREAM_CODEC.listOf()).map(SpawnCubeParticlesPayload::new, SpawnCubeParticlesPayload::map));

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
		ctx.level().cubeParticles(map);
	}
}
