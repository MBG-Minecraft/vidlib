package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.particle.ShapeParticleOptions;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Map;

public record SpawnShapeParticlesPayload(Map<ShapeParticleOptions, List<BlockPos>> map) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SpawnShapeParticlesPayload> TYPE = SimplePayloadType.vidlib("spawn_shape_particles", KLibStreamCodecs.unboundedMap(ShapeParticleOptions.STREAM_CODEC, KLibStreamCodecs.listOf(BlockPos.STREAM_CODEC)).map(SpawnShapeParticlesPayload::new, SpawnShapeParticlesPayload::map));

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
		ctx.features().shapeParticles(map);
	}
}
