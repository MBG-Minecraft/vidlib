package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LineParticleOptions(Color startColor, Color endColor, int ttl) implements ParticleOptions {
	public static final MapCodec<LineParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Color.CODEC.optionalFieldOf("start_color", Color.WHITE).forGetter(LineParticleOptions::startColor),
		Color.CODEC.optionalFieldOf("end_color", Color.CYAN).forGetter(LineParticleOptions::endColor),
		Codec.INT.optionalFieldOf("ttl", 40).forGetter(LineParticleOptions::ttl)
	).apply(instance, LineParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LineParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		Color.STREAM_CODEC, LineParticleOptions::startColor,
		Color.STREAM_CODEC, LineParticleOptions::endColor,
		ByteBufCodecs.VAR_INT, LineParticleOptions::ttl,
		LineParticleOptions::new
	);

	public LineParticleOptions(Color color, int ttl) {
		this(color, color, ttl);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.LINE.get();
	}
}
