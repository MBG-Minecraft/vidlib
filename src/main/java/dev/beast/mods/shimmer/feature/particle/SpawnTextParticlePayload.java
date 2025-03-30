package dev.beast.mods.shimmer.feature.particle;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SpawnTextParticlePayload(TextParticleOptions options, List<Vec3> positions) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SpawnTextParticlePayload> TYPE = ShimmerPacketType.internal("spawn_text_particle", CompositeStreamCodec.of(
		TextParticleOptions.STREAM_CODEC, SpawnTextParticlePayload::options,
		Vec3.STREAM_CODEC.list(), SpawnTextParticlePayload::positions,
		SpawnTextParticlePayload::new
	));

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
		ctx.level().textParticles(options, positions);
	}
}
