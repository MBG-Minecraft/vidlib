package dev.latvian.mods.vidlib.feature.bulk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface BlockModificationConsumer {
	void set(BlockPos pos, BlockState state);

	default void add(BulkLevelModification modification) {
		modification.apply(this);
	}

	default void set(BlockPos pos, Block state) {
		set(pos, state.defaultBlockState());
	}

	default void fill(BlockPos start, BlockPos end, BlockState state) {
		for (var pos : BlockPos.betweenClosed(start, end)) {
			set(pos, state);
		}
	}

	default void fill(BlockPos start, BlockPos end, Block state) {
		fill(start, end, state.defaultBlockState());
	}

	default void fillSection(SectionPos pos, BlockState state) {
		fill(pos.origin(), pos.origin().offset(15, 15, 15), state);
	}

	default void fillSection(SectionPos pos, Block state) {
		fillSection(pos, state.defaultBlockState());
	}

	default void applyPalettes(SectionPos pos, List<BlockPalette> palettes) {
		var blockPos = new BlockPos.MutableBlockPos();

		for (var palette : palettes) {
			for (int i = 0; i < palette.positions().size(); i++) {
				SectionData.getIndex(blockPos, pos, palette.positions().getShort(i) & 0xFFFF);
				set(blockPos, palette.state());
			}
		}
	}
}
