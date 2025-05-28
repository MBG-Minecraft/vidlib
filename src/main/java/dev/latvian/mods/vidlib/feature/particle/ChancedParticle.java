package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
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

	public static final DataType<ChancedParticle> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, ChancedParticle.class);
	public static final RegisteredDataType<ChancedParticle> KNOWN_CODEC = RegisteredDataType.register(VidLib.id("chanced_particle"), DATA_TYPE);
	public static final RegisteredDataType<List<ChancedParticle>> LIST_KNOWN_CODEC = KNOWN_CODEC.listOf();
}
