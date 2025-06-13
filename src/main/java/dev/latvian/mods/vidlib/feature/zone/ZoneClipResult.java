package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ZoneClipResult(ZoneInstance instance, ZoneShape shape, double distanceSq, @Nullable Vec3 pos, Object result) {
	public static ZoneClipResult of(ZoneInstance instance, ZoneShape shape, Line ray, HitResult hit) {
		return new ZoneClipResult(instance, shape, hit.getLocation().distanceToSqr(ray.start()), hit.getLocation(), hit);
	}

	public static ZoneClipResult of(ZoneInstance instance, ZoneShape shape, Line ray, Vec3 pos) {
		return new ZoneClipResult(instance, shape, pos.distanceToSqr(ray.start()), pos, null);
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
