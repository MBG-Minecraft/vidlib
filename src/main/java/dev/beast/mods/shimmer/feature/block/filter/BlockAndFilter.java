package dev.beast.mods.shimmer.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.ArrayList;
import java.util.List;

public record BlockAndFilter(List<BlockFilter> filters) implements BlockFilter {
	public static SimpleRegistryType<BlockAndFilter> TYPE = SimpleRegistryType.dynamic(Shimmer.id("and"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockAndFilter::filters)
	).apply(instance, BlockAndFilter::new)), BlockFilter.STREAM_CODEC.list().map(BlockAndFilter::new, BlockAndFilter::filters));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		for (var filter : filters) {
			if (!filter.test(block)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		for (var filter : filters) {
			if (!filter.test(level, pos, state)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public BlockFilter and(BlockFilter filter) {
		var list = new ArrayList<>(filters);
		list.add(filter);
		return new BlockAndFilter(List.copyOf(list));
	}
}
