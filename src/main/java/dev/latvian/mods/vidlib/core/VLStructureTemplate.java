package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public interface VLStructureTemplate {
	default void fillBlocksFromWorld(Level level, Stream<BlockPos> blocks, BlockFilter filter) {
		throw new NoMixinException(this);
	}
}
