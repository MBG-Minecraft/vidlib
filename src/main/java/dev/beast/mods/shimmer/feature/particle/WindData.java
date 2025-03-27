package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@AutoInit
public record WindData(
	WindParticleOptions options,
	ParticleMovementData data
) {
	public static final Codec<WindData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WindParticleOptions.CODEC.fieldOf("options").forGetter(WindData::options),
		ParticleMovementData.CODEC.fieldOf("data").forGetter(WindData::data)
	).apply(instance, WindData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WindData> STREAM_CODEC = CompositeStreamCodec.of(
		WindParticleOptions.STREAM_CODEC, WindData::options,
		ParticleMovementData.STREAM_CODEC, WindData::data,
		WindData::new
	);

	public static final KnownCodec<WindData> KNOWN_CODEC = KnownCodec.register(Shimmer.id("wind"), CODEC, STREAM_CODEC, WindData.class);
}
