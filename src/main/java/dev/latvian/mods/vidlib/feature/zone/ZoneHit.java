package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ZoneHit(
	Ref<Zone> zone,
	@Nullable ResolvedZone resolvedZone,
	Vec3 pos,
	double distanceSq,
	@Nullable Direction direction
) {
	public BlockHitResult asBlockHitResult() {
		return new BlockHitResult(pos, direction == null ? Direction.UP : direction, BlockPos.containing(pos), false);
	}
}
