package dev.beast.mods.shimmer.math;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record Line(Vec3 start, Vec3 end) {
	public double dx() {
		return end.x() - start.x();
	}

	public double dy() {
		return end.y() - start.y();
	}

	public double dz() {
		return end.z() - start.z();
	}

	@Nullable
	public BlockHitResult hitBlock(Player player, ClipContext.Fluid fluids) {
		var result = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, fluids, player));
		return result.getType() == HitResult.Type.BLOCK ? result : null;
	}
}
