package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerServerSessionData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public interface ShimmerServerPlayer extends ShimmerPlayer {
	@Override
	default ShimmerServerSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}

	@Override
	default void s2c(Packet<? super ClientGamePacketListener> packet) {
		((ServerPlayer) this).connection.send(packet);
	}

	@Override
	default GameType getGameMode() {
		return ((ServerPlayer) this).gameMode.getGameModeForPlayer();
	}
}
