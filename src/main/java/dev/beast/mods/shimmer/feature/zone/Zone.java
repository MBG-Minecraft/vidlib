package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.util.Cast;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Zone {
	Codec<Zone> CODEC = ZoneType.CODEC.dispatch("type", Zone::type, ZoneType::codec);
	StreamCodec<ByteBuf, Zone> STREAM_CODEC = ZoneType.STREAM_CODEC.dispatch(Zone::type, type -> Cast.to(type.streamCodec()));

	ZoneType<?> type();

	default boolean canMove() {
		return false;
	}

	AABB getBoundingBox();

	@Nullable
	default ZoneClipResult clip(Vec3 start, Vec3 end) {
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

	default boolean contains(AABB box) {
		return getBoundingBox().intersects(box);
	}
}
