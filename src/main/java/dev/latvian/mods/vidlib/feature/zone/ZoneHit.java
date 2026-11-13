package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
	public ZoneHitResult asBlockHitResult() {
		return new ZoneHitResult(this);
	}
}
