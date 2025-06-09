package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record FollowingEntityWorldVector(Either<Integer, UUID> entityId, EntityPositionType positionType) implements WorldVector {
	public static final SimpleRegistryType<FollowingEntityWorldVector> TYPE = SimpleRegistryType.dynamic("following_entity", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.either(Codec.INT, KLibCodecs.UUID).fieldOf("entity_id").forGetter(FollowingEntityWorldVector::entityId),
		EntityPositionType.CODEC.optionalFieldOf("position_type", EntityPositionType.CENTER).forGetter(FollowingEntityWorldVector::positionType)
	).apply(instance, FollowingEntityWorldVector::new)), CompositeStreamCodec.of(
		ByteBufCodecs.either(ByteBufCodecs.VAR_INT, KLibStreamCodecs.UUID), FollowingEntityWorldVector::entityId,
		EntityPositionType.STREAM_CODEC, FollowingEntityWorldVector::positionType,
		FollowingEntityWorldVector::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var entity = ctx.level.getEntityByEither(entityId);
		return entity == null ? null : entity.getPosition(positionType);
	}
}
