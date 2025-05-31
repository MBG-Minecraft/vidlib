package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CubeParticleOptions(Gradient color, Gradient lineColor, int ttl) implements ParticleOptions {
	public static final MapCodec<CubeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("color", Color.CYAN).forGetter(CubeParticleOptions::color),
		Gradient.CODEC.optionalFieldOf("line_color", Color.WHITE).forGetter(CubeParticleOptions::lineColor),
		Codec.INT.optionalFieldOf("ttl", 40).forGetter(CubeParticleOptions::ttl)
	).apply(instance, CubeParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CubeParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, CubeParticleOptions::color,
		Gradient.STREAM_CODEC, CubeParticleOptions::lineColor,
		ByteBufCodecs.VAR_INT, CubeParticleOptions::ttl,
		CubeParticleOptions::new
	);

	public CubeParticleOptions(Gradient color, int ttl) {
		this(color, Color.TRANSPARENT, ttl);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.CUBE.get();
	}
}
