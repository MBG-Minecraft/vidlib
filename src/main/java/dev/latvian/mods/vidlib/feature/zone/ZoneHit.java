package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.math.HitBuilder;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ZoneHit(
	Ref<Zone> zone,
	@Nullable ResolvedZone resolvedZone,
	Vec3 pos,
	double distanceSq,
	@Nullable Direction direction,
	BlockPos blockPos
) {
	public static ZoneHit from(Ref<Zone> zone, @Nullable ResolvedZone resolvedZone, HitBuilder hit) {
		int bx = Mth.floor(hit.hit.x);
		int by = Mth.floor(hit.hit.y);
		int bz = Mth.floor(hit.hit.z);

		if (hit.direction == Direction.EAST) {
			bx--;
		} else if (hit.direction == Direction.UP) {
			by--;
		} else if (hit.direction == Direction.SOUTH) {
			bz--;
		}

		return new ZoneHit(zone, resolvedZone, hit.hit, hit.distanceSq, hit.direction, new BlockPos(bx, by, bz));
	}

	public ZoneHitResult asBlockHitResult() {
		return new ZoneHitResult(this);
	}
}
