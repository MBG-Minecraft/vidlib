package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LineParticleOptions(int lifespan, Gradient startColor, Gradient endColor, int endOffset) implements ParticleOptions {
	public static final MapCodec<LineParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 40).forGetter(LineParticleOptions::lifespan),
		Gradient.CODEC.optionalFieldOf("start_color", Color.WHITE).forGetter(LineParticleOptions::startColor),
		Gradient.CODEC.optionalFieldOf("end_color", Color.CYAN).forGetter(LineParticleOptions::endColor),
		Codec.INT.optionalFieldOf("end_offset", 0).forGetter(LineParticleOptions::endOffset)
	).apply(instance, LineParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LineParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, LineParticleOptions::lifespan,
		Gradient.STREAM_CODEC, LineParticleOptions::startColor,
		Gradient.STREAM_CODEC, LineParticleOptions::endColor,
		ByteBufCodecs.VAR_INT, LineParticleOptions::endOffset,
		LineParticleOptions::new
	);

	public LineParticleOptions(int lifespan, Gradient startColor, Gradient endColor) {
		this(lifespan, startColor, endColor, 0);
	}

	public LineParticleOptions(int lifespan, Gradient color) {
		this(lifespan, color, color);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.LINE.get();
	}
}
