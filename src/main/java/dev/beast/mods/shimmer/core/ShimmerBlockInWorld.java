package dev.beast.mods.shimmer.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public interface ShimmerBlockInWorld {
	static BlockInWorld of(Level level, BlockPos pos, BlockState state) {
		var block = new BlockInWorld(level, pos, true);
		((ShimmerBlockInWorld) block).shimmer$setState(state);
		return block;
	}

	default void shimmer$setState(BlockState state) {
	}
}
