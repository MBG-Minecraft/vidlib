package dev.latvian.mods.vidlib.feature.prop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PropHitResult extends BlockHitResult {
	public final Prop prop;

	public PropHitResult(Prop prop, Vec3 location, Direction direction, BlockPos blockPos, boolean inside) {
		super(location, direction, blockPos, inside, false);
		this.prop = prop;
	}
}
