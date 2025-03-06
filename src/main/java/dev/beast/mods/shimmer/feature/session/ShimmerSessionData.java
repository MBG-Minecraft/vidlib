package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.serverdata.ServerData;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShimmerSessionData {
	public final UUID uuid;
	Map<PlayerDataType<?>, PlayerDataMapValue> playerDataMap;

	public ShimmerSessionData(UUID uuid) {
		this.uuid = uuid;
	}

	PlayerDataMapValue init(PlayerDataType<?> type) {
		if (playerDataMap == null) {
			playerDataMap = new IdentityHashMap<>(1);
		}

		var v = playerDataMap.get(type);

		if (v == null) {
			v = new PlayerDataMapValue();
			v.playerData = type.factory().get();
			playerDataMap.put(type, v);
		}

		return v;
	}

	@SuppressWarnings("unchecked")
	public <T extends PlayerData> T get(PlayerDataType<T> type) {
		return (T) init(type).playerData;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends PlayerData> T getOrNull(PlayerDataType<T> type) {
		if (playerDataMap == null) {
			return null;
		}

		var v = playerDataMap.get(type);

		if (v == null) {
			return null;
		}

		return (T) v.playerData;
	}

	public void respawned(Level level, boolean loggedIn) {
	}

	public void closed() {
	}

	public void updateZones(Level level, List<ZoneContainer> update) {
	}

	public void updateClockFonts(List<ClockFont> update) {
	}

	public void updateClocks(Level level, List<ClockInstance> update) {
	}

	public void updateClockInstance(ResourceLocation id, int tick, boolean ticking) {
	}

	public void updateSessionData(UUID ownId, UUID player, List<PlayerData> playerData) {
	}

	public void removeSessionData(UUID id) {
	}

	public void updatePlayerTags(UUID ownId, UUID uuid, List<String> tags) {
	}

	public void updateServerData(List<ServerData> serverData) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + uuid + "]";
	}
}
