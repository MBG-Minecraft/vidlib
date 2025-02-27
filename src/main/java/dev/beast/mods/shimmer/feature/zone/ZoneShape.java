package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.MapCodec;
import dev.beast.mods.shimmer.util.Cast;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ZoneShape {
	MapCodec<ZoneShape> CODEC = ZoneShapeType.CODEC.dispatchMap("type", ZoneShape::type, ZoneShapeType::codec);
	StreamCodec<ByteBuf, ZoneShape> STREAM_CODEC = ZoneShapeType.STREAM_CODEC.dispatch(ZoneShape::type, type -> Cast.to(type.streamCodec()));

	ZoneShapeType<?> type();

	default ZoneInstance createInstance(ZoneContainer container, Zone zone) {
		return new ZoneInstance(container, zone);
	}

	default boolean canMove() {
		return false;
	}

	AABB getBoundingBox();

	@Nullable
	default ZoneClipResult clip(Vec3 start, Vec3 end) {
		if (contains(start)) {
			return null;
		}

		var result = AABB.clip(List.of(getBoundingBox()), start, end, BlockPos.ZERO);

		if (result != null && result.getType() == HitResult.Type.BLOCK) {
			var pos = result.getLocation();
			return new ZoneClipResult(this, pos.distanceToSqr(start), pos, result);
		}

		return null;
	}

	default boolean contains(Vec3 pos) {
		return getBoundingBox().contains(pos);
	}

	default boolean intersects(AABB box) {
		return getBoundingBox().intersects(box);
	}

	default Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(getBoundingBox());
	}

	default List<Entity> collectEntities(Level level, Predicate<? super Entity> predicate) {
		return level.getEntities((Entity) null, getBoundingBox(), predicate);
	}
}
