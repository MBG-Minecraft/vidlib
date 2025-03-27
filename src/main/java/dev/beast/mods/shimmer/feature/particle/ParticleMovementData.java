package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ParticleMovementData(
	ParticleMovementType type,
	BlockPos position,
	int count,
	float radius,
	float yaw
) {
	public static final Codec<ParticleMovementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ParticleMovementType.KNOWN_CODEC.codec().optionalFieldOf("type", ParticleMovementType.CIRCULAR).forGetter(ParticleMovementData::type),
		BlockPos.CODEC.fieldOf("position").forGetter(ParticleMovementData::position),
		Codec.INT.optionalFieldOf("count", 1).forGetter(ParticleMovementData::count),
		Codec.FLOAT.optionalFieldOf("radius", 20F).forGetter(ParticleMovementData::radius),
		Codec.FLOAT.optionalFieldOf("yaw", 0F).forGetter(ParticleMovementData::yaw)
	).apply(instance, ParticleMovementData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ParticleMovementData> STREAM_CODEC = CompositeStreamCodec.of(
		ParticleMovementType.KNOWN_CODEC.streamCodec().optional(ParticleMovementType.CIRCULAR), ParticleMovementData::type,
		BlockPos.STREAM_CODEC, ParticleMovementData::position,
		ByteBufCodecs.VAR_INT, ParticleMovementData::count,
		ByteBufCodecs.FLOAT, ParticleMovementData::radius,
		ByteBufCodecs.FLOAT, ParticleMovementData::yaw,
		ParticleMovementData::new
	);
}
