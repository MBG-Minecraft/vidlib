package dev.latvian.mods.vidlib.feature.explosion;

import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

public record DestroyedBlock(BlockPos pos, BlockState state, int dx, int dy, int dz, float inside, MutableBoolean destroyed) implements Comparable<DestroyedBlock> {
	@Override
	public int compareTo(@NotNull DestroyedBlock o) {
		return Double.compare(inside, o.inside);
	}

	public PositionedBlock toPositionedBlock() {
		return new PositionedBlock(pos, state);
	}
}
