package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public interface ShimmerStructureTemplate {
	default void fillBlocksFromWorld(Level level, Stream<BlockPos> blocks, BlockFilter filter) {
		throw new NoMixinException(this);
	}
}
