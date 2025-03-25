package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ChancedParticle(ParticleOptions particle, float chance) {
	public static final Codec<ChancedParticle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ParticleTypes.CODEC.fieldOf("particle").forGetter(ChancedParticle::particle),
		Codec.FLOAT.optionalFieldOf("chance", 1F).forGetter(ChancedParticle::chance)
	).apply(instance, ChancedParticle::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ChancedParticle> STREAM_CODEC = CompositeStreamCodec.of(
		ParticleTypes.STREAM_CODEC, ChancedParticle::particle,
		ByteBufCodecs.FLOAT, ChancedParticle::chance,
		ChancedParticle::new
	);

	public static final KnownCodec<ChancedParticle> KNOWN_CODEC = KnownCodec.register(Shimmer.id("chanced_particle"), CODEC, STREAM_CODEC, ChancedParticle.class);
	public static final KnownCodec<List<ChancedParticle>> LIST_KNOWN_CODEC = KnownCodec.register(Shimmer.id("chanced_particle_list"), CODEC.listOf(), STREAM_CODEC.list(), Cast.to(List.class));
}
