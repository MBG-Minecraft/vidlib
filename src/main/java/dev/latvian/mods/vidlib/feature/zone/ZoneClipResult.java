package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ZoneClipResult(Zone zone, double distanceSq, @Nullable Vec3 pos, Object result) {
	public static ZoneClipResult of(ZoneClipContext ctx, HitResult hit) {
		return new ZoneClipResult(ctx.zone, hit.getLocation().distanceToSqr(ctx.getFrom()), hit.getLocation(), hit);
	}

	public static ZoneClipResult of(ZoneClipContext ctx, Vec3 pos) {
		return new ZoneClipResult(ctx.zone, pos.distanceToSqr(ctx.getFrom()), pos, null);
	}

	@Nullable
	public BlockHitResult asBlockHitResult() {
		if (result instanceof BlockHitResult blockHit) {
			return blockHit;
		} else if (pos != null) {
			return new BlockHitResult(pos, Direction.UP, BlockPos.containing(pos), false);
		} else {
			return null;
		}
	}
}
