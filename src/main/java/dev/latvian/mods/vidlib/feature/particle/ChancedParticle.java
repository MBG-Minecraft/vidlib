package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ChancedParticle(ParticleOptions particle, KNumber chance) {
	public static final Codec<ChancedParticle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ParticleTypes.CODEC.fieldOf("particle").forGetter(ChancedParticle::particle),
		KNumber.CODEC.optionalFieldOf("chance", KNumber.ONE).forGetter(ChancedParticle::chance)
	).apply(instance, ChancedParticle::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ChancedParticle> STREAM_CODEC = CompositeStreamCodec.of(
		ParticleTypes.STREAM_CODEC, ChancedParticle::particle,
		KNumber.STREAM_CODEC, ChancedParticle::chance,
		ChancedParticle::new
	);

	public static final DataType<ChancedParticle> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, ChancedParticle.class);
	public static final DataType<List<ChancedParticle>> LIST_DATA_TYPE = DATA_TYPE.listOf();
}
