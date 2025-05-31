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

public record BrightCubeParticleOptions(Gradient color, Gradient lineColor, int ttl) implements ParticleOptions {
	public static final MapCodec<BrightCubeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(BrightCubeParticleOptions::color),
		Gradient.CODEC.optionalFieldOf("line_color", Color.CYAN).forGetter(BrightCubeParticleOptions::lineColor),
		Codec.INT.optionalFieldOf("ttl", 40).forGetter(BrightCubeParticleOptions::ttl)
	).apply(instance, BrightCubeParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BrightCubeParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, BrightCubeParticleOptions::color,
		Gradient.STREAM_CODEC, BrightCubeParticleOptions::lineColor,
		ByteBufCodecs.VAR_INT, BrightCubeParticleOptions::ttl,
		BrightCubeParticleOptions::new
	);

	public BrightCubeParticleOptions(Gradient color, int ttl) {
		this(color, color, ttl);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.BRIGHT_CUBE.get();
	}
}
