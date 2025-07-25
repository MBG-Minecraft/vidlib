package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LightningParticleOptions(int lifespan, Gradient color, Gradient outlineColor, int segments, float spread, float radius, float endingRadius) implements ParticleOptions {
	public static final Color DEFAULT_OUTLINE = Color.of(0xFF80BFFF);

	public static final MapCodec<LightningParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		KLibCodecs.TICKS.optionalFieldOf("lifespan", 30).forGetter(LightningParticleOptions::lifespan),
		Gradient.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(LightningParticleOptions::color),
		Gradient.CODEC.optionalFieldOf("outline_color", DEFAULT_OUTLINE).forGetter(LightningParticleOptions::outlineColor),
		Codec.INT.optionalFieldOf("segments", 6).forGetter(LightningParticleOptions::segments),
		Codec.FLOAT.optionalFieldOf("spread", 2.3F).forGetter(LightningParticleOptions::spread),
		Codec.FLOAT.optionalFieldOf("radius", 0.04F).forGetter(LightningParticleOptions::radius),
		Codec.FLOAT.optionalFieldOf("ending_radius", 0F).forGetter(LightningParticleOptions::endingRadius)
	).apply(instance, LightningParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LightningParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, LightningParticleOptions::lifespan,
		Gradient.STREAM_CODEC, LightningParticleOptions::color,
		Gradient.STREAM_CODEC, LightningParticleOptions::outlineColor,
		ByteBufCodecs.VAR_INT, LightningParticleOptions::segments,
		ByteBufCodecs.FLOAT, LightningParticleOptions::spread,
		ByteBufCodecs.FLOAT, LightningParticleOptions::radius,
		KLibStreamCodecs.optional(ByteBufCodecs.FLOAT, 0F), LightningParticleOptions::endingRadius,
		LightningParticleOptions::new
	);

	@Override
	public ParticleType<?> getType() {
		return VidLibParticles.LIGHTNING.get();
	}
}
