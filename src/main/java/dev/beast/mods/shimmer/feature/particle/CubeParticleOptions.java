package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.kmath.color.Color;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CubeParticleOptions(Color color, Color lineColor, int ttl) implements ParticleOptions {
	public static final MapCodec<CubeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.optionalFieldOf("color", Color.CYAN).forGetter(CubeParticleOptions::color),
		Color.CODEC.optionalFieldOf("line_color", Color.WHITE).forGetter(CubeParticleOptions::lineColor),
		Codec.INT.optionalFieldOf("ttl", 40).forGetter(CubeParticleOptions::ttl)
	).apply(instance, CubeParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CubeParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		Color.STREAM_CODEC, CubeParticleOptions::color,
		Color.STREAM_CODEC, CubeParticleOptions::lineColor,
		ByteBufCodecs.VAR_INT, CubeParticleOptions::ttl,
		CubeParticleOptions::new
	);

	public CubeParticleOptions(Color color, int ttl) {
		this(color, Color.TRANSPARENT, ttl);
	}

	@Override
	public ParticleType<?> getType() {
		return ShimmerParticles.CUBE.get();
	}
}
