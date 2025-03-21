package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.core.ShimmerS2CPacketConsumer;
import dev.beast.mods.shimmer.feature.data.SyncPlayerDataPayload;
import dev.beast.mods.shimmer.feature.data.SyncServerDataPayload;
import dev.beast.mods.shimmer.feature.input.PlayerInputChanged;
import dev.beast.mods.shimmer.feature.input.SyncPlayerInputToClient;
import dev.beast.mods.shimmer.feature.misc.RefreshNamePayload;
import dev.beast.mods.shimmer.feature.misc.SyncPlayerTagsPayload;
import dev.beast.mods.shimmer.util.S2CPacketBundleBuilder;
import dev.beast.mods.shimmer.util.registry.SyncRegistryPayload;
import dev.beast.mods.shimmer.util.registry.SyncedRegistry;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Map;

public class ShimmerServerSessionData extends ShimmerSessionData {
	public final ServerGamePacketListenerImpl connection;

	public ShimmerServerSessionData(ServerGamePacketListenerImpl connection) {
		super(connection.player.getUUID());
		this.connection = connection;
	}

	public void shimmer$preTick(ServerPlayer player) {
		updateOverrides(player);
	}

	public void shimmer$postTick(ShimmerS2CPacketConsumer packetsToEveryone, ServerPlayer player) {
		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChanged(player, prevInput, input));
			prevInput = input;
			packetsToEveryone.s2c(new SyncPlayerInputToClient(player.getUUID(), input));
		}

		dataMap.sync(packetsToEveryone, player, SyncPlayerDataPayload::new);
	}

	public void sync(S2CPacketBundleBuilder packets, ServerPlayer player, boolean login) {
		var level = player.serverLevel();
		packets.s2c(new ClientboundSetTimePacket(level.getGameTime(), level.getDayTime(), level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));

		for (var reg : SyncedRegistry.ALL.values()) {
			packets.s2c(new SyncRegistryPayload(reg, Map.copyOf(reg.registry().getMap())));
		}

		dataMap.load(player.server, player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("shimmer").resolve(player.getUUID() + ".nbt"));

		if (login) {
			player.refreshDisplayName();
			player.refreshTabListName();
		}

		updateOverrides(player);

		player.server.getServerData().syncAll(packets, null, (uuid, updates) -> new SyncServerDataPayload(updates));
		dataMap.syncAll(packets, player, SyncPlayerDataPayload::new);

		if (login) {
			packets.s2c(new RefreshNamePayload(player.getUUID()));
		}

		for (var p : player.server.getPlayerList().getPlayers()) {
			packets.s2c(new SyncPlayerTagsPayload(p.getUUID(), List.copyOf(p.getTags())));

			if (!p.getUUID().equals(player.getUUID())) {
				var s = p.shimmer$sessionData();

				packets.s2c(new SyncPlayerInputToClient(p.getUUID(), s.input));
				s.dataMap.syncAll(packets, null, SyncPlayerDataPayload::new);
			}
		}
	}
}
