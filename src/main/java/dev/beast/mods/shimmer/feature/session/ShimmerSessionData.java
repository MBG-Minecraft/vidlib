package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class ShimmerSessionData {
	public final UUID uuid;
	public final DataMap dataMap;

	public ShimmerSessionData(UUID uuid) {
		this.uuid = uuid;
		this.dataMap = new DataMap(DataType.PLAYER);
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

	public void updateSessionData(Player self, UUID player, List<DataMapValue> playerData) {
	}

	public void removeSessionData(UUID id) {
	}

	public void updatePlayerTags(UUID ownId, UUID uuid, List<String> tags) {
	}

	public void updateServerData(List<DataMapValue> serverData) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + uuid + "]";
	}

	public void refreshBlockZones() {
	}
}
