package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public interface ShimmerServerPlayer extends ShimmerPlayer {
	@Override
	default void s2c(Packet<?> packet) {
		((ServerPlayer) this).connection.send(packet);
	}

	@Override
	default GameType getGameMode() {
		return ((ServerPlayer) this).gameMode.getGameModeForPlayer();
	}
}
