package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;

public record BlockOrFilter(List<BlockFilter> filters) implements BlockFilter {
	public static SimpleRegistryType<BlockOrFilter> TYPE = SimpleRegistryType.dynamic("or", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.listOf().fieldOf("filters").forGetter(BlockOrFilter::filters)
	).apply(instance, BlockOrFilter::new)), BlockFilter.STREAM_CODEC.list().map(BlockOrFilter::new, BlockOrFilter::filters));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		for (var filter : filters) {
			if (filter.test(block)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		for (var filter : filters) {
			if (filter.test(level, pos, state)) {
				return true;
			}
		}

		return false;
	}
}
