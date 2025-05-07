package dev.latvian.mods.vidlib.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockNotFilter(BlockFilter filter) implements BlockFilter {
	public static SimpleRegistryType<BlockNotFilter> TYPE = SimpleRegistryType.dynamic("not", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockFilter.CODEC.fieldOf("filter").forGetter(BlockNotFilter::filter)
	).apply(instance, BlockNotFilter::new)), BlockFilter.STREAM_CODEC.map(BlockNotFilter::new, BlockNotFilter::filter));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return !filter.test(block);
	}

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return !filter.test(level, pos, state);
	}

	@Override
	public BlockFilter not() {
		return filter;
	}
}
