package dev.beast.mods.shimmer.feature.block.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlockStateFilter(BlockState blockState) implements BlockFilter {
	public static final SimpleRegistryType<BlockStateFilter> TYPE = SimpleRegistryType.dynamic(Shimmer.id("block_state"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BlockState.CODEC.fieldOf("block_state").forGetter(BlockStateFilter::blockState)
	).apply(instance, BlockStateFilter::new)), ShimmerStreamCodecs.BLOCK_STATE.map(BlockStateFilter::new, BlockStateFilter::blockState));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(BlockInWorld block) {
		return block.getState() == blockState;
	}
}
