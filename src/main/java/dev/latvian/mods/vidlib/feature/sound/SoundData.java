package dev.latvian.mods.vidlib.feature.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public record SoundData(
	Holder<SoundEvent> sound,
	SoundSource source,
	float volume,
	float pitch
) {
	public static final Codec<SoundData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		SoundEvent.CODEC.fieldOf("sound").forGetter(SoundData::sound),
		MCCodecs.SOUND_SOURCE.optionalFieldOf("source", SoundSource.PLAYERS).forGetter(SoundData::source),
		Codec.FLOAT.optionalFieldOf("volume", 1F).forGetter(SoundData::volume),
		Codec.FLOAT.optionalFieldOf("pitch", 1F).forGetter(SoundData::pitch)
	).apply(instance, SoundData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SoundData> STREAM_CODEC = CompositeStreamCodec.of(
		SoundEvent.STREAM_CODEC, SoundData::sound,
		DataTypes.SOUND_SOURCE.streamCodec(), SoundData::source,
		ByteBufCodecs.FLOAT, SoundData::volume,
		ByteBufCodecs.FLOAT, SoundData::pitch,
		SoundData::new
	);

	public static final DataType<SoundData> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, SoundData.class);

	public SoundData(Holder<SoundEvent> sound, float volume, float pitch) {
		this(sound, SoundSource.PLAYERS, volume, pitch);
	}

	public SoundData(Holder<SoundEvent> sound, float volume) {
		this(sound, volume, 1F);
	}

	public SoundData(Holder<SoundEvent> sound) {
		this(sound, 1F);
	}

	public SoundData(SoundEvent sound, SoundSource source, float volume, float pitch) {
		this(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source, volume, pitch);
	}

	public SoundData(SoundEvent sound, float volume, float pitch) {
		this(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), volume, pitch);
	}

	public SoundData(SoundEvent sound, float volume) {
		this(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), volume);
	}

	public SoundData(SoundEvent sound) {
		this(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound));
	}
}
