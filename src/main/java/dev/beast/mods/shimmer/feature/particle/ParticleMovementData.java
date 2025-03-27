package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.math.MovementType;
import dev.beast.mods.shimmer.math.Rotation;
import dev.beast.mods.shimmer.math.Vec3f;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

public record ParticleMovementData(
	MovementType type,
	BlockPos position,
	int count,
	float radius,
	float deviate,
	Rotation rotation
) {
	public static final Codec<ParticleMovementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		MovementType.KNOWN_CODEC.codec().optionalFieldOf("type", MovementType.CIRCULAR).forGetter(ParticleMovementData::type),
		BlockPos.CODEC.fieldOf("position").forGetter(ParticleMovementData::position),
		Codec.INT.optionalFieldOf("count", 1).forGetter(ParticleMovementData::count),
		Codec.FLOAT.optionalFieldOf("radius", 20F).forGetter(ParticleMovementData::radius),
		Codec.FLOAT.optionalFieldOf("deviate", 0F).forGetter(ParticleMovementData::deviate),
		Rotation.CODEC.optionalFieldOf("yaw", Rotation.NONE).forGetter(ParticleMovementData::rotation)
	).apply(instance, ParticleMovementData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ParticleMovementData> STREAM_CODEC = CompositeStreamCodec.of(
		MovementType.KNOWN_CODEC.streamCodec().optional(MovementType.CIRCULAR), ParticleMovementData::type,
		BlockPos.STREAM_CODEC, ParticleMovementData::position,
		ByteBufCodecs.VAR_INT, ParticleMovementData::count,
		ByteBufCodecs.FLOAT, ParticleMovementData::radius,
		ByteBufCodecs.FLOAT, ParticleMovementData::deviate,
		Rotation.STREAM_CODEC_NO_ROLL.optional(Rotation.NONE), ParticleMovementData::rotation,
		ParticleMovementData::new
	);

	public Vec3f delta(RandomSource random) {
		return type.delta(random, radius, deviate, rotation);
	}
}
