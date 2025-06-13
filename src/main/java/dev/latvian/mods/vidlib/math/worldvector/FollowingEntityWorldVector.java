package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.IntOrUUID;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingEntityWorldVector(IntOrUUID entityId, EntityPositionType positionType) implements WorldVector {
	public static final SimpleRegistryType<FollowingEntityWorldVector> TYPE = SimpleRegistryType.dynamic("following_entity", RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntOrUUID.DATA_TYPE.codec().fieldOf("entity_id").forGetter(FollowingEntityWorldVector::entityId),
		EntityPositionType.CODEC.optionalFieldOf("position_type", EntityPositionType.CENTER).forGetter(FollowingEntityWorldVector::positionType)
	).apply(instance, FollowingEntityWorldVector::new)), CompositeStreamCodec.of(
		IntOrUUID.DATA_TYPE.streamCodec(), FollowingEntityWorldVector::entityId,
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
		var entity = ctx.level.getEntity(entityId);
		return entity == null ? null : entity.getPosition(positionType);
	}
}
