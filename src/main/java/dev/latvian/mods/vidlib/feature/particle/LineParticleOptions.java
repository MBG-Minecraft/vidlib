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

public record LineParticleOptions(Gradient startColor, Gradient endColor, int ttl) implements ParticleOptions {
	public static final MapCodec<LineParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Gradient.CODEC.optionalFieldOf("start_color", Color.WHITE).forGetter(LineParticleOptions::startColor),
		Gradient.CODEC.optionalFieldOf("end_color", Color.CYAN).forGetter(LineParticleOptions::endColor),
		Codec.INT.optionalFieldOf("ttl", 40).forGetter(LineParticleOptions::ttl)
	).apply(instance, LineParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LineParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, LineParticleOptions::startColor,
		Gradient.STREAM_CODEC, LineParticleOptions::endColor,
		ByteBufCodecs.VAR_INT, LineParticleOptions::ttl,
		LineParticleOptions::new
	);

	public LineParticleOptions(Gradient color, int ttl) {
		this(color, color, ttl);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.LINE.get();
	}
}
