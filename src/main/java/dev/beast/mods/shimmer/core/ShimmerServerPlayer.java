package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerServerSessionData;
import dev.beast.mods.shimmer.util.MiscUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public interface ShimmerServerPlayer extends ShimmerPlayer {
	@Override
	default ShimmerServerSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}

	@Override
	default void s2c(@Nullable Packet<? super ClientGamePacketListener> packet) {
		if (packet instanceof ClientboundBundlePacket bundle) {
			var collection = MiscUtils.toSequencedCollection(bundle.subPackets());

			if (collection.isEmpty()) {
				return;
			} else if (collection.size() == 1) {
				((ServerPlayer) this).connection.send(collection.getFirst());
				return;
			}
		}

		if (packet != null) {
			((ServerPlayer) this).connection.send(packet);
		}
	}

	@Override
	default GameType getGameMode() {
		return ((ServerPlayer) this).gameMode.getGameModeForPlayer();
	}
}
