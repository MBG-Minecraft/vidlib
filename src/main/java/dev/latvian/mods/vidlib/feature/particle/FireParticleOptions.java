package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.interpolation.EaseOut;
import dev.latvian.mods.klib.interpolation.Interpolation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FireParticleOptions(int lifespan, Gradient color, float scale, Interpolation interpolation, float brightness) implements ParticleOptions {
	public static final MapCodec<FireParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 60).forGetter(FireParticleOptions::lifespan),
		Gradient.CODEC.fieldOf("color").forGetter(FireParticleOptions::color),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(FireParticleOptions::scale),
		Interpolation.CODEC.optionalFieldOf("easing", EaseOut.SINE).forGetter(FireParticleOptions::interpolation),
		Codec.FLOAT.optionalFieldOf("brightness", 0.4F).forGetter(FireParticleOptions::brightness)
	).apply(instance, FireParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FireParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, FireParticleOptions::lifespan,
		Gradient.STREAM_CODEC, FireParticleOptions::color,
		ByteBufCodecs.FLOAT, FireParticleOptions::scale,
		Interpolation.STREAM_CODEC, FireParticleOptions::interpolation,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0.4F), FireParticleOptions::brightness,
		FireParticleOptions::new
	);

	public FireParticleOptions(int lifespan, Gradient gradient, float scale) {
		this(lifespan, gradient, scale, EaseOut.SINE, 0.4F);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.FIRE.get();
	}

	public FireParticleOptions withResolvedGradient() {
		return new FireParticleOptions(lifespan, color.optimize(), scale, interpolation, brightness);
	}
}
