package dev.beast.mods.shimmer.feature.particle;

import com.mojang.datafixers.util.Pair;
import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SpawnItemParticlePayload(ItemParticleOptions options, List<Pair<Vec3, Vec3>> positions) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SpawnItemParticlePayload> TYPE = ShimmerPacketType.internal("spawn_item_particles", CompositeStreamCodec.of(
		ItemParticleOptions.STREAM_CODEC, SpawnItemParticlePayload::options,
		ShimmerStreamCodecs.pair(Vec3.STREAM_CODEC, Vec3.STREAM_CODEC).list(), SpawnItemParticlePayload::positions,
		SpawnItemParticlePayload::new
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
		ctx.level().itemParticles(options, positions);
	}
}
