package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.misc.FakeBlockPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public interface ShimmerServerLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return ((ServerLevel) this).getServer();
	}

	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToPlayersInDimension((ServerLevel) this, packet);
	}

	@Override
	default void setFakeBlock(BlockPos pos, BlockState state) {
		send(new FakeBlockPayload(pos, state));
	}
}
