package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.misc.SyncPlayerTagsPayload;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VLServerPlayer extends VLPlayer {
	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return List.of((Player) this);
	}

	@Override
	default ServerSessionData vl$sessionData() {
		throw new NoMixinException(this);
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

	default void vl$initialSync(PacketAndPayloadAcceptor<ClientGamePacketListener> callback) {
		var p = (ServerPlayer) this;
		callback.accept(new SyncPlayerTagsPayload(p.getUUID(), List.copyOf(p.getTags())).toS2C(p.level()));
	}
}
