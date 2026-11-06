package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToClient;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.prop.PropRemoveType;
import dev.latvian.mods.vidlib.feature.prop.RemoveAllPropsPayload;
import dev.latvian.mods.vidlib.feature.zone.ResolvedZone;
import dev.latvian.mods.vidlib.feature.zone.ZoneTag;
import dev.latvian.mods.vidlib.math.knumber.SyncGlobalNumberVariablesPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerSessionData implements Comparable<PlayerSessionData> {
	public final WorldSessionData worldData;
	public final UUID uuid;
	public final DataMap dataMap;
	public final long startTime;
	public PlayerInput prevInput;
	public PlayerInput input;
	public List<ResolvedZone> zonesIn;
	public Set<ZoneTag> zoneTagsIn;

	public boolean suspended;

	public PlayerSessionData(WorldSessionData worldData, UUID uuid) {
		this.worldData = worldData;
		this.uuid = uuid;
		this.dataMap = new DataMap(worldData, uuid, DataKey.PLAYER);
		this.startTime = System.currentTimeMillis();
		this.prevInput = PlayerInput.NONE;
		this.input = PlayerInput.NONE;
		this.zonesIn = List.of();
		this.zoneTagsIn = Set.of();

		this.suspended = false;
	}

	public void closed() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + uuid + "]";
	}


	/**
	 * syncType 0 = reload
	 * syncType 1 = replay snapshot
	 * syncType 2 = login
	 */
	public void sync(S2CPacketConsumer packets, Player player, int syncType) {
		var level = player.level();
		var environment = level.getEnvironment();

		if (level.getServer() != null) {
			packets.s2c(level.getServer().clockManager().createFullSyncPacket());
		}
		// packets.s2c(new ServerFeaturesPayload(FeatureSet.SERVER_FEATURES.get()));

		CustomRegistry.syncAll(player, packets);

		packets.s2c(new SyncGlobalNumberVariablesPayload(environment.globalVariables()));

		updateOverrides(player);

		if (syncType > 0) {
			player.refreshDisplayName();

			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.refreshTabListName();
			}
		}

		environment.sync(packets);

		dataMap.syncAll(packets, SyncPlayerDataPayload::new);

		for (var s : environment.vl$getAllSessionData()) {
			if (!s.uuid.equals(player.getUUID())) {
				packets.s2c(new SyncPlayerInputToClient(s.uuid, s.input));
				s.dataMap.syncAll(packets, SyncPlayerDataPayload::new);
			}
		}

		if (syncType > 0) {
			var props = VLPlatformHelper.INSTANCE.getProps(level);

			if (syncType == 1) {
				packets.s2c(new RemoveAllPropsPayload(PropRemoveType.REPLAY));
			}

			for (var prop : props.levelProps) {
				packets.s2c(prop.createAddPacket());
			}
		}
	}

	public Set<String> getTags() {
		return dataMap.get(InternalPlayerData.PLAYER_TAGS);
	}

	public void setTags(Set<String> tags) {
		if (!getTags().equals(tags)) {
			dataMap.set(InternalPlayerData.PLAYER_TAGS, tags);
		}
	}

	public boolean isOnline() {
		return dataMap.get(InternalPlayerData.ONLINE);
	}

	public String getName() {
		var name = dataMap.get(InternalPlayerData.NAME);

		if (name.isEmpty()) {
			name = PlayerProfiles.getName(uuid);
			dataMap.set(InternalPlayerData.NAME, name);
		}

		return name;
	}

	public void updateOverrides(Player player) {
		suspended = dataMap.get(InternalPlayerData.SUSPENDED);
	}

	@Override
	public int compareTo(@NotNull PlayerSessionData o) {
		return getName().compareToIgnoreCase(o.getName());
	}
}
