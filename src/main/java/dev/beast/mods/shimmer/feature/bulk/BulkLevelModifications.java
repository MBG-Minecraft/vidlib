package dev.beast.mods.shimmer.feature.bulk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record BulkLevelModifications(List<BulkLevelModification> modifications) implements BlockModificationConsumer {
	@Override
	public void add(BulkLevelModification modification) {
		modifications.add(modification);
	}

	@Override
	public void set(BlockPos pos, BlockState state) {
		modifications.add(new ReplaceSingleBlock(pos, state));
	}

	@Override
	public void fill(BlockPos start, BlockPos end, BlockState state) {
		modifications.add(new ReplaceCuboidBlocks(start, end, state));
	}
}
