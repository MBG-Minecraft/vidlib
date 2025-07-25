package dev.latvian.mods.vidlib.feature.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.PositionType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public record PositionedSoundData(SoundData data, Optional<KVector> position, boolean looping, boolean stopImmediately) {
	public static final Codec<PositionedSoundData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SoundData.CODEC.fieldOf("data").forGetter(PositionedSoundData::data),
		KVector.CODEC.optionalFieldOf("position").forGetter(PositionedSoundData::position),
		Codec.BOOL.optionalFieldOf("looping", false).forGetter(PositionedSoundData::looping),
		Codec.BOOL.optionalFieldOf("stop_immediately", false).forGetter(PositionedSoundData::stopImmediately)
	).apply(instance, PositionedSoundData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PositionedSoundData> STREAM_CODEC = CompositeStreamCodec.of(
		SoundData.STREAM_CODEC, PositionedSoundData::data,
		ByteBufCodecs.optional(KVector.STREAM_CODEC), PositionedSoundData::position,
		ByteBufCodecs.BOOL, PositionedSoundData::looping,
		ByteBufCodecs.BOOL, PositionedSoundData::stopImmediately,
		PositionedSoundData::new
	);

	public static final DataType<PositionedSoundData> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, PositionedSoundData.class);

	public PositionedSoundData(SoundData data) {
		this(data, Optional.empty(), false, false);
	}

	public PositionedSoundData(SoundData data, KVector position, boolean looping, boolean stopImmediately) {
		this(data, Optional.of(position), looping, stopImmediately);
	}

	public PositionedSoundData(SoundData data, Entity entity, boolean looping, boolean stopImmediately) {
		this(data, KVector.following(entity, PositionType.SOUND_SOURCE), looping, stopImmediately);
	}

	public PositionedSoundData(SoundData data, Prop prop, boolean looping, boolean stopImmediately) {
		this(data, KVector.following(prop, PositionType.SOUND_SOURCE), looping, stopImmediately);
	}
}
