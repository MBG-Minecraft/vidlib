package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SpawnTextParticlePayload(TextParticleOptions options, List<Vec3> positions) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SpawnTextParticlePayload> TYPE = VidLibPacketType.internal("spawn_text_particles", CompositeStreamCodec.of(
		TextParticleOptions.STREAM_CODEC, SpawnTextParticlePayload::options,
		Vec3.STREAM_CODEC.listOf(), SpawnTextParticlePayload::positions,
		SpawnTextParticlePayload::new
	));

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
		ctx.level().textParticles(options, positions);
	}
}
