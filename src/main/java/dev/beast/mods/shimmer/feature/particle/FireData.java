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
public record FireData(
	FireParticleOptions options,
	ParticleMovementData data
) {
	public static final Codec<FireData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		FireParticleOptions.CODEC.fieldOf("options").forGetter(FireData::options),
		ParticleMovementData.CODEC.fieldOf("data").forGetter(FireData::data)
	).apply(instance, FireData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FireData> STREAM_CODEC = CompositeStreamCodec.of(
		FireParticleOptions.STREAM_CODEC, FireData::options,
		ParticleMovementData.STREAM_CODEC, FireData::data,
		FireData::new
	);

	public static final KnownCodec<FireData> KNOWN_CODEC = KnownCodec.register(Shimmer.id("fire"), CODEC, STREAM_CODEC, FireData.class);
}
