package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record SpawnItemParticlePayload(ItemParticleOptions options, List<Pair<Vec3, Vec3>> positions) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SpawnItemParticlePayload> TYPE = VidLibPacketType.internal("spawn_item_particles", CompositeStreamCodec.of(
		ItemParticleOptions.STREAM_CODEC, SpawnItemParticlePayload::options,
		VLStreamCodecs.pair(Vec3.STREAM_CODEC, Vec3.STREAM_CODEC).listOf(), SpawnItemParticlePayload::positions,
		SpawnItemParticlePayload::new
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
		ctx.level().itemParticles(options, positions);
	}
}
