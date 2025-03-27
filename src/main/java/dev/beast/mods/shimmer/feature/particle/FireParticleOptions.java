package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.math.Easing;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record FireParticleOptions(ResourceLocation gradient, int lifespan, float scale, Easing easing) implements ParticleOptions {
	public static final MapCodec<FireParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ShimmerCodecs.VIDEO_ID.fieldOf("gradient").forGetter(FireParticleOptions::gradient),
		Codec.INT.optionalFieldOf("lifespan", 100).forGetter(FireParticleOptions::lifespan),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(FireParticleOptions::scale),
		Easing.CODEC.optionalFieldOf("easing", Easing.SINE_OUT).forGetter(FireParticleOptions::easing)
	).apply(instance, FireParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FireParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ShimmerStreamCodecs.VIDEO_ID, FireParticleOptions::gradient,
		ByteBufCodecs.VAR_INT, FireParticleOptions::lifespan,
		ByteBufCodecs.FLOAT, FireParticleOptions::scale,
		Easing.STREAM_CODEC.optional(Easing.SINE_OUT), FireParticleOptions::easing,
		FireParticleOptions::new
	);

	public FireParticleOptions(ResourceLocation gradient, int lifespan, float scale) {
		this(gradient, lifespan, scale, Easing.SINE_OUT);
	}

	@Override
	public ParticleType<?> getType() {
		return ShimmerParticles.WIND.get();
	}
}
