package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;

public record SpawnLineParticlesPayload(Map<LineParticleOptions, List<AABB>> map) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SpawnLineParticlesPayload> TYPE = VidLibPacketType.internal("spawn_line_particles", KLibStreamCodecs.unboundedMap(LineParticleOptions.STREAM_CODEC, KLibStreamCodecs.listOf(MCStreamCodecs.AABB)).map(SpawnLineParticlesPayload::new, SpawnLineParticlesPayload::map));

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
		ctx.level().lineParticles(map);
	}
}
