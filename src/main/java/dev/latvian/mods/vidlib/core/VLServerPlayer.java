package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.misc.RefreshNamePayload;
import dev.latvian.mods.vidlib.feature.misc.SyncPlayerTagsPayload;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.network.chat.Component;
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
	default ServerPlayer vl$self() {
		return (ServerPlayer) this;
	}

	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return List.of(vl$self());
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
				vl$self().connection.send(collection.getFirst());
				return;
			}
		}

		if (packet != null) {
			vl$self().connection.send(packet);
		}
	}

	@Override
	default GameType getGameMode() {
		return vl$self().gameMode.getGameModeForPlayer();
	}

	default void vl$initialSync(PacketAndPayloadAcceptor<ClientGamePacketListener> callback) {
		var p = vl$self();
		callback.accept(new SyncPlayerTagsPayload(p.getUUID(), List.copyOf(p.getTags())).toS2C(p.level()));
	}

	default void updateNickname(Component name) {
		var player = vl$self();
		player.setNickname(name);
		player.refreshDisplayName();
		player.refreshTabListName();
		player.server.s2c(new RefreshNamePayload(player.getUUID(), player.getNickname()));
	}
}
