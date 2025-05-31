package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FireParticleOptions(Gradient color, int lifespan, float scale, Easing easing, float brightness) implements ParticleOptions {
	public static final MapCodec<FireParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Gradient.CODEC.fieldOf("color").forGetter(FireParticleOptions::color),
		Codec.INT.optionalFieldOf("lifespan", 60).forGetter(FireParticleOptions::lifespan),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(FireParticleOptions::scale),
		Easing.CODEC.optionalFieldOf("easing", Easing.SINE_OUT).forGetter(FireParticleOptions::easing),
		Codec.FLOAT.optionalFieldOf("brightness", 0.4F).forGetter(FireParticleOptions::brightness)
	).apply(instance, FireParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FireParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		Gradient.STREAM_CODEC, FireParticleOptions::color,
		ByteBufCodecs.VAR_INT, FireParticleOptions::lifespan,
		ByteBufCodecs.FLOAT, FireParticleOptions::scale,
		Easing.STREAM_CODEC.optional(Easing.SINE_OUT), FireParticleOptions::easing,
		ByteBufCodecs.FLOAT.optional(0.4F), FireParticleOptions::brightness,
		FireParticleOptions::new
	);

	public FireParticleOptions(Gradient gradient, int lifespan, float scale) {
		this(gradient, lifespan, scale, Easing.SINE_OUT, 0.4F);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.FIRE.get();
	}

	public FireParticleOptions withResolvedGradient() {
		return new FireParticleOptions(color.resolve(), lifespan, scale, easing, brightness);
	}
}
