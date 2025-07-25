package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.easing.Easing;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FireParticleOptions(int lifespan, Gradient color, float scale, Easing easing, float brightness) implements ParticleOptions {
	public static final MapCodec<FireParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 60).forGetter(FireParticleOptions::lifespan),
		Gradient.CODEC.fieldOf("color").forGetter(FireParticleOptions::color),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(FireParticleOptions::scale),
		Easing.CODEC.optionalFieldOf("easing", Easing.SINE_OUT).forGetter(FireParticleOptions::easing),
		Codec.FLOAT.optionalFieldOf("brightness", 0.4F).forGetter(FireParticleOptions::brightness)
	).apply(instance, FireParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FireParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, FireParticleOptions::lifespan,
		Gradient.STREAM_CODEC, FireParticleOptions::color,
		ByteBufCodecs.FLOAT, FireParticleOptions::scale,
		Easing.STREAM_CODEC, FireParticleOptions::easing,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0.4F), FireParticleOptions::brightness,
		FireParticleOptions::new
	);

	public FireParticleOptions(int lifespan, Gradient gradient, float scale) {
		this(lifespan, gradient, scale, Easing.SINE_OUT, 0.4F);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.FIRE.get();
	}

	public FireParticleOptions withResolvedGradient() {
		return new FireParticleOptions(lifespan, color.optimize(), scale, easing, brightness);
	}
}
