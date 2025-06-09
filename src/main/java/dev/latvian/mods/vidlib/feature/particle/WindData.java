package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

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
}
