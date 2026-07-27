package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.EasingType;

public record WindParticleOptions(int lifespan, boolean ground, float scale, EasingType ease) implements ParticleOptions {
	public static final MapCodec<WindParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 100).forGetter(WindParticleOptions::lifespan),
		Codec.BOOL.optionalFieldOf("ground", false).forGetter(WindParticleOptions::ground),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(WindParticleOptions::scale),
		EasingType.CODEC.optionalFieldOf("ease", EasingType.OUT_SINE).forGetter(WindParticleOptions::ease)
	).apply(instance, WindParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WindParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, WindParticleOptions::lifespan,
		ByteBufCodecs.BOOL, WindParticleOptions::ground,
		ByteBufCodecs.FLOAT, WindParticleOptions::scale,
		MCStreamCodecs.EASING_TYPE, WindParticleOptions::ease,
		WindParticleOptions::new
	);

	public WindParticleOptions(int lifespan, boolean ground, float scale) {
		this(lifespan, ground, scale, EasingType.OUT_SINE);
	}

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.WIND.get();
	}
}
