package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

public record SpawnCubeParticlesPayload(Map<ShapeParticleOptions, List<BlockPos>> map) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SpawnCubeParticlesPayload> TYPE = SimplePayloadType.vidlib("spawn_cube_particles", KLibStreamCodecs.unboundedMap(ShapeParticleOptions.STREAM_CODEC, KLibStreamCodecs.listOf(BlockPos.STREAM_CODEC)).map(SpawnCubeParticlesPayload::new, SpawnCubeParticlesPayload::map));

	@Override
	public SimplePayloadType<?> getType() {
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
