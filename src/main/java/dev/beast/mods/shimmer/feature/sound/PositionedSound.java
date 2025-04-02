package dev.beast.mods.shimmer.feature.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record PositionedSound(SoundData data, Optional<WorldPosition> position, boolean looping) {
	public static final Codec<PositionedSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SoundData.CODEC.fieldOf("data").forGetter(PositionedSound::data),
		WorldPosition.CODEC.optionalFieldOf("position").forGetter(PositionedSound::position),
		Codec.BOOL.optionalFieldOf("looping", false).forGetter(PositionedSound::looping)
	).apply(instance, PositionedSound::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PositionedSound> STREAM_CODEC = CompositeStreamCodec.of(
		SoundData.STREAM_CODEC, PositionedSound::data,
		WorldPosition.STREAM_CODEC.optional(), PositionedSound::position,
		ByteBufCodecs.BOOL, PositionedSound::looping,
		PositionedSound::new
	);
}
