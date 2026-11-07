package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ZoneHitResult extends BlockHitResult {
	public final Ref<Zone> zone;
	public final @Nullable ResolvedZone resolvedZone;

	public ZoneHitResult(ZoneHit hit) {
		super(hit.pos(), hit.direction() == null ? Direction.UP : hit.direction(), BlockPos.containing(hit.pos()), false, false);
		this.zone = hit.zone();
		this.resolvedZone = hit.resolvedZone();
	}
}
