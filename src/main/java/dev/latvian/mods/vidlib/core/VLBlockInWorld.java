package dev.latvian.mods.vidlib.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public interface VLBlockInWorld {
	static BlockInWorld of(Level level, BlockPos pos, BlockState state) {
		var block = new BlockInWorld(level, pos, true);
		((VLBlockInWorld) block).vl$setState(state);
		return block;
	}

	default void vl$setState(BlockState state) {
	}
}
