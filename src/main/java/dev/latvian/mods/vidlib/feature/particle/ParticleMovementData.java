package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.MovementType;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.kmath.Vec3f;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public record ParticleMovementData(
	MovementType type,
	Vec3 position,
	int count,
	float radius,
	float deviate,
	Rotation rotation
) {
	public static final Codec<ParticleMovementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KnownCodec.MOVEMENT_TYPE.codec().optionalFieldOf("type", MovementType.CIRCULAR).forGetter(ParticleMovementData::type),
		Vec3.CODEC.fieldOf("position").forGetter(ParticleMovementData::position),
		Codec.INT.optionalFieldOf("count", 1).forGetter(ParticleMovementData::count),
		Codec.FLOAT.optionalFieldOf("radius", 20F).forGetter(ParticleMovementData::radius),
		Codec.FLOAT.optionalFieldOf("deviate", 0F).forGetter(ParticleMovementData::deviate),
		Rotation.CODEC.optionalFieldOf("yaw", Rotation.NONE).forGetter(ParticleMovementData::rotation)
	).apply(instance, ParticleMovementData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ParticleMovementData> STREAM_CODEC = CompositeStreamCodec.of(
		KnownCodec.MOVEMENT_TYPE.streamCodec().optional(MovementType.CIRCULAR), ParticleMovementData::type,
		Vec3.STREAM_CODEC, ParticleMovementData::position,
		ByteBufCodecs.VAR_INT, ParticleMovementData::count,
		ByteBufCodecs.FLOAT, ParticleMovementData::radius,
		ByteBufCodecs.FLOAT, ParticleMovementData::deviate,
		Rotation.STREAM_CODEC_NO_ROLL.optional(Rotation.NONE), ParticleMovementData::rotation,
		ParticleMovementData::new
	);

	public ParticleMovementData(MovementType type, Vec3 position, int count, float radius) {
		this(type, position, count, radius, 0F, Rotation.NONE);
	}

	public Vec3f delta(RandomSource random) {
		return type.delta(random, radius, deviate, rotation);
	}
}
