package dev.beast.mods.shimmer.feature.block;

import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.math.Directions;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record ConnectedBlock(PositionedBlock block, int distance) {
	public enum WalkType {
		DEFAULT(false, false),
		DIAGONAL(true, false),
		HORIZONTAL(false, true),
		HORIZONTAL_DIAGONAL(true, true);

		public final List<BlockPos> offsets;

		WalkType(boolean diagonal, boolean horizontal) {
			var offsets = new ArrayList<BlockPos>();

			if (!diagonal && !horizontal) {
				for (var dir : Directions.ALL) {
					offsets.add(new BlockPos(dir.getStepX(), dir.getStepY(), dir.getStepZ()));
				}
			} else {
				int yoff = horizontal ? 0 : 1;

				for (int y = -yoff; y <= yoff; y++) {
					if (diagonal) {
						for (int x = -1; x <= 1; x++) {
							for (int z = -1; z <= 1; z++) {
								if (x != 0 || y != 0 || z != 0) {
									offsets.add(new BlockPos(x, y, z));
								}
							}
						}
					} else {
						for (var dir : Directions.HORIZONTAL) {
							offsets.add(new BlockPos(dir.getStepX(), y, dir.getStepZ()));
						}
					}
				}
			}

			this.offsets = List.copyOf(offsets);
		}
	}
}
