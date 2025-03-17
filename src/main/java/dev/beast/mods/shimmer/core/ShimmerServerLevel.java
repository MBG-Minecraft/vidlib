package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationPayload;
import dev.beast.mods.shimmer.feature.bulk.ReplaceSectionBlocks;
import dev.beast.mods.shimmer.feature.misc.FakeBlockPayload;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface ShimmerServerLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return ((ServerLevel) this).getServer();
	}

	default void shimmer$setActiveZones(ActiveZones zones) {
	}

	@Override
	default void setFakeBlock(BlockPos pos, BlockState state) {
		s2c(new FakeBlockPayload(pos, state));
	}

	@Override
	default int bulkModify(BulkLevelModification modification) {
		var optimized = modification.optimize();

		if (optimized instanceof BulkLevelModificationBundle bundle) {
			var sections = new ReplaceSectionBlocks.Builder();

			for (var m : bundle.list()) {
				m.apply(sections);
			}

			optimized = sections.build();
		}

		s2c(new BulkLevelModificationPayload(optimized));
		return ShimmerLevel.super.bulkModify(optimized);
	}
}
