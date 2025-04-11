package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.kmath.color.Color;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TextParticleOptions(Component text, Color color, int ttl, float distance, float scale, boolean seeThrough) implements ParticleOptions {
	public static final MapCodec<TextParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ComponentSerialization.CODEC.fieldOf("text").forGetter(TextParticleOptions::text),
		Color.CODEC.optionalFieldOf("color", Color.WHITE).forGetter(TextParticleOptions::color),
		Codec.INT.optionalFieldOf("ttl", 40).forGetter(TextParticleOptions::ttl),
		Codec.FLOAT.optionalFieldOf("distance", 16F).forGetter(TextParticleOptions::distance),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(TextParticleOptions::scale),
		Codec.BOOL.optionalFieldOf("see_through", false).forGetter(TextParticleOptions::seeThrough)
	).apply(instance, TextParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TextParticleOptions> STREAM_CODEC = CompositeStreamCodec.of(
		ComponentSerialization.TRUSTED_STREAM_CODEC, TextParticleOptions::text,
		Color.STREAM_CODEC, TextParticleOptions::color,
		ByteBufCodecs.VAR_INT, TextParticleOptions::ttl,
		ByteBufCodecs.FLOAT, TextParticleOptions::distance,
		ByteBufCodecs.FLOAT, TextParticleOptions::scale,
		ByteBufCodecs.BOOL, TextParticleOptions::seeThrough,
		TextParticleOptions::new
	);

	public TextParticleOptions(Component text, int ttl) {
		this(text, Color.WHITE, ttl, 16F, 1F, false);
	}

	@Override
	public ParticleType<?> getType() {
		return ShimmerParticles.TEXT.get();
	}
}
