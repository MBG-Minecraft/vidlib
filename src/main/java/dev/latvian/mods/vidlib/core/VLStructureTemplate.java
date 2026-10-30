package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.block.filter.BlockFilter;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public interface VLStructureTemplate {
	default void fillBlocksFromWorld(Level level, Stream<BlockPos> blocks, Ref<BlockFilter> filter) {
		throw new NoMixinException(this);
	}
}
