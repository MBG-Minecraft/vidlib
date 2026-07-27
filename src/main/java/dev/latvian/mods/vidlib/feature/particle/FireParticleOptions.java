package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.EasingType;

public record FireParticleOptions(int lifespan, Ref<Gradient> color, float scale, EasingType ease, float brightness) implements ParticleOptions {
	public static final MapCodec<FireParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 60).forGetter(FireParticleOptions::lifespan),
		Gradient.CODEC.fieldOf("color").forGetter(FireParticleOptions::color),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(FireParticleOptions::scale),
		EasingType.CODEC.optionalFieldOf("ease", EasingType.OUT_SINE).forGetter(FireParticleOptions::ease),
		Codec.FLOAT.optionalFieldOf("brightness", 0.4F).forGetter(FireParticleOptions::brightness)
	).apply(instance, FireParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FireParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, FireParticleOptions::lifespan,
		Gradient.STREAM_CODEC, FireParticleOptions::color,
		ByteBufCodecs.FLOAT, FireParticleOptions::scale,
		MCStreamCodecs.EASING_TYPE, FireParticleOptions::ease,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0.4F), FireParticleOptions::brightness,
		FireParticleOptions::new
	);

	public FireParticleOptions(int lifespan, Ref<Gradient> gradient, float scale) {
		this(lifespan, gradient, scale, EasingType.OUT_SINE, 0.4F);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.FIRE.get();
	}
}
