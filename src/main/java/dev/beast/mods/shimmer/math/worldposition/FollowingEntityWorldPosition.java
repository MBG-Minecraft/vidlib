package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.ShimmerCodecs;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record FollowingEntityWorldPosition(Either<Integer, UUID> entityId, WorldPosition fallback, EntityPositionType positionType) implements WorldPosition {
	public static final SimpleRegistryType<FollowingEntityWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("following_entity"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.either(Codec.INT, ShimmerCodecs.UUID).fieldOf("entity_id").forGetter(FollowingEntityWorldPosition::entityId),
		WorldPosition.CODEC.fieldOf("fallback").forGetter(FollowingEntityWorldPosition::fallback),
		EntityPositionType.CODEC.optionalFieldOf("position_type", EntityPositionType.CENTER).forGetter(FollowingEntityWorldPosition::positionType)
	).apply(instance, FollowingEntityWorldPosition::new)), StreamCodec.composite(
		ByteBufCodecs.either(ByteBufCodecs.VAR_INT, ShimmerStreamCodecs.UUID),
		FollowingEntityWorldPosition::entityId,
		WorldPosition.STREAM_CODEC,
		FollowingEntityWorldPosition::fallback,
		EntityPositionType.STREAM_CODEC,
		FollowingEntityWorldPosition::positionType,
		FollowingEntityWorldPosition::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(Level level, float progress) {
		var entity = level.getEntityByEither(entityId);
		return entity == null ? fallback.get(level, progress) : positionType.getPosition(entity);
	}
}
