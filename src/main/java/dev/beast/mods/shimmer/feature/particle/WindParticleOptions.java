package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.math.Easing;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WindParticleOptions(int lifespan, boolean ground, Easing easing) implements ParticleOptions {
	public static final MapCodec<WindParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.INT.optionalFieldOf("lifespan", 100).forGetter(WindParticleOptions::lifespan),
		Codec.BOOL.optionalFieldOf("ground", false).forGetter(WindParticleOptions::ground),
		Easing.CODEC.optionalFieldOf("easing", Easing.EXPO_OUT).forGetter(WindParticleOptions::easing)
	).apply(instance, WindParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WindParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, WindParticleOptions::lifespan,
		ByteBufCodecs.BOOL, WindParticleOptions::ground,
		Easing.STREAM_CODEC.optional(Easing.EXPO_OUT), WindParticleOptions::easing,
		WindParticleOptions::new
	);

	@Override
	public ParticleType<?> getType() {
		return ShimmerParticles.WIND.get();
	}
}
