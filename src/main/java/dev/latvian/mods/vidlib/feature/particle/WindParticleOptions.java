package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.easing.Easing;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WindParticleOptions(int lifespan, boolean ground, float scale, Easing easing) implements ParticleOptions {
	public static final MapCodec<WindParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 100).forGetter(WindParticleOptions::lifespan),
		Codec.BOOL.optionalFieldOf("ground", false).forGetter(WindParticleOptions::ground),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(WindParticleOptions::scale),
		Easing.CODEC.optionalFieldOf("easing", Easing.SINE_OUT).forGetter(WindParticleOptions::easing)
	).apply(instance, WindParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WindParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, WindParticleOptions::lifespan,
		ByteBufCodecs.BOOL, WindParticleOptions::ground,
		ByteBufCodecs.FLOAT, WindParticleOptions::scale,
		Easing.STREAM_CODEC, WindParticleOptions::easing,
		WindParticleOptions::new
	);

	public WindParticleOptions(int lifespan, boolean ground, float scale) {
		this(lifespan, ground, scale, Easing.SINE_OUT);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.WIND.get();
	}
}
