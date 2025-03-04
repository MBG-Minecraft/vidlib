package dev.beast.mods.shimmer.core;

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
}
