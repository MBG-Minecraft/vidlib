package dev.latvian.mods.vidlib.feature.block.filter;

import dev.latvian.mods.klib.block.filter.SimpleBlockPredicate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Predicate;

public abstract class SimpleBlockFilter implements BlockFilter {
	public static SimpleBlockFilter simple(SimpleRegistryType<BlockFilter> type, SimpleBlockPredicate predicate) {
		return new SimpleBlockFilter(type) {
			@Override
			public boolean test(BlockInWorld block) {
				var state = block.getState();
				return state != null && predicate.test(block.getLevel(), block.getPos(), state);
			}

			@Override
			public boolean test(LevelReader level, BlockPos pos, BlockState state) {
				return predicate.test(level, pos, state);
			}
		};
	}

	public static SimpleBlockFilter simple(SimpleRegistryType<BlockFilter> type, Predicate<BlockState> predicate) {
		return new SimpleBlockFilter(type) {
			@Override
			public boolean test(BlockInWorld block) {
				var state = block.getState();
				return state != null && predicate.test(state);
			}

			@Override
			public boolean test(LevelReader level, BlockPos pos, BlockState state) {
				return predicate.test(state);
			}
		};
	}

	private final SimpleRegistryType<BlockFilter> type;

	public SimpleBlockFilter(SimpleRegistryType<BlockFilter> type) {
		this.type = type;
	}

	@Override
	public SimpleRegistryType<?> type() {
		return type;
	}

	@Override
	public String toString() {
		return type.id();
	}
}
