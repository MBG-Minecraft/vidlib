package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingEntityWorldVector(EntityFilter entity, EntityPositionType positionType) implements WorldVector {
	public static final SimpleRegistryType<FollowingEntityWorldVector> TYPE = SimpleRegistryType.dynamic("following_entity", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.fieldOf("entity").forGetter(FollowingEntityWorldVector::entity),
		EntityPositionType.CODEC.optionalFieldOf("position_type", EntityPositionType.CENTER).forGetter(FollowingEntityWorldVector::positionType)
	).apply(instance, FollowingEntityWorldVector::new)), CompositeStreamCodec.of(
		EntityFilter.STREAM_CODEC, FollowingEntityWorldVector::entity,
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
		var e = entity.getFirst(ctx.level);
		return e == null ? null : e.getPosition(positionType);
	}
}
