package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.core.VLS2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChanged;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToClient;
import dev.latvian.mods.vidlib.feature.misc.RefreshNamePayload;
import dev.latvian.mods.vidlib.feature.misc.SyncPlayerTagsPayload;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropRemoveType;
import dev.latvian.mods.vidlib.feature.prop.RemoveAllPropsPayload;
import dev.latvian.mods.vidlib.feature.registry.SyncRegistryPayload;
import dev.latvian.mods.vidlib.feature.registry.SyncedRegistry;
import dev.latvian.mods.vidlib.math.worldnumber.SyncGlobalNumberVariablesPayload;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerSessionData extends SessionData {
	public ServerSessionData(UUID uuid) {
		super(uuid);
	}

	public void vl$preTick(ServerPlayer player) {
		updateOverrides(player);
	}

	public void vl$postTick(VLS2CPacketConsumer packetsToEveryone, ServerPlayer player) {
		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChanged(player, prevInput, input));
			prevInput = input;
			packetsToEveryone.s2c(new SyncPlayerInputToClient(player.getUUID(), input));
		}

		if (!player.level().isReplayLevel()) {
			dataMap.sync(packetsToEveryone, player, SyncPlayerDataPayload::new);
		}

		tick++;
	}

	public void load(ServerPlayer player) {
		dataMap.load(player.server, player.server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("vidlib").resolve(player.getUUID() + ".nbt"));
	}

	public void sync(S2CPacketBundleBuilder packets, ServerPlayer player, boolean login) {
		var level = player.serverLevel();
		packets.s2c(new ClientboundSetTimePacket(level.getGameTime(), level.getDayTime(), level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));

		for (var reg : SyncedRegistry.ALL.values()) {
			packets.s2c(new SyncRegistryPayload(reg, Map.copyOf(reg.registry().getMap())));
		}

		packets.s2c(new SyncGlobalNumberVariablesPayload(player.server.globalVariables()));

		updateOverrides(player);

		if (login) {
			player.refreshDisplayName();
			player.refreshTabListName();
		}

		player.server.sync(packets, player);
		dataMap.syncAll(packets, player, SyncPlayerDataPayload::new);

		if (login) {
			packets.s2c(new RefreshNamePayload(player.getUUID(), player.getNickname()));
		}

		for (var p : player.server.getPlayerList().getPlayers()) {
			packets.s2c(new SyncPlayerTagsPayload(p.getUUID(), List.copyOf(p.getTags())));

			if (!p.getUUID().equals(player.getUUID())) {
				var s = p.vl$sessionData();

				packets.s2c(new SyncPlayerInputToClient(p.getUUID(), s.input));
				s.dataMap.syncAll(packets, null, SyncPlayerDataPayload::new);
			}
		}

		if (login) {
			for (var list : level.getProps().propLists.values()) {
				packets.s2c(new RemoveAllPropsPayload(list.type, PropRemoveType.LOGIN));

				for (var prop : list) {
					packets.s2c(prop.createAddPacket());
				}
			}
		}
	}
}
