package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.ZoneClipResult;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import dev.beast.mods.shimmer.feature.zone.shape.ZoneShape;
import dev.beast.mods.shimmer.math.VoxelShapeBox;
import dev.beast.mods.shimmer.util.Side;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShimmerLocalClientSessionData extends ShimmerClientSessionData {
	public final ActiveZones serverZones;
	public final ActiveZones filteredZones;
	private final Map<UUID, ShimmerRemoteClientSessionData> remoteSessionData;
	public final Set<String> tags;
	public ZoneClipResult zoneClip;
	public Map<ResourceLocation, ClockFont> clockFonts;
	public Map<ResourceLocation, ClockInstance> clocks;
	public final DataMap serverDataMap;
	public Map<ZoneShape, VoxelShapeBox> cachedZoneShapes;

	public ShimmerLocalClientSessionData(UUID uuid) {
		super(uuid);
		Shimmer.LOGGER.info("Client Session Data Initialized");
		this.serverZones = new ActiveZones();
		this.filteredZones = new ActiveZones();
		this.remoteSessionData = new HashMap<>();
		this.tags = new HashSet<>(0);
		this.clockFonts = Map.of();
		this.clocks = Map.of();
		this.serverDataMap = new DataMap(DataType.SERVER);
	}

	public ShimmerRemoteClientSessionData getRemoteSessionData(UUID id) {
		var data = remoteSessionData.get(id);

		if (data == null) {
			data = new ShimmerRemoteClientSessionData(id);
			remoteSessionData.put(id, data);
		}

		return data;
	}

	@Override
	public void respawned(Level level, boolean loggedIn) {
		refreshZones(level.dimension());
	}

	@Override
	public void closed() {
		ClockFont.CLIENT_SUPPLIER = Map::of;
	}

	public void refreshZones(ResourceKey<Level> dimension) {
		filteredZones.filter(dimension, serverZones);
		cachedZoneShapes = null;
		NeoForge.EVENT_BUS.post(new ZoneEvent.Updated(dimension, filteredZones, Side.CLIENT));
	}

	@Override
	public void updateZones(Level level, List<ZoneContainer> update) {
		serverZones.update(update);
		NeoForge.EVENT_BUS.post(new ZoneEvent.AllUpdated(serverZones, Side.SERVER));
		refreshZones(level.dimension());
	}

	@Override
	public void updateClockFonts(List<ClockFont> update) {
		var map = new HashMap<ResourceLocation, ClockFont>();

		for (var font : update) {
			map.put(font.id(), font);
		}

		clockFonts = Map.copyOf(map);
		ClockFont.CLIENT_SUPPLIER = () -> clockFonts;
	}

	@Override
	public void updateClocks(Level level, List<ClockInstance> update) {
		var map = new HashMap<ResourceLocation, ClockInstance>();

		for (var clock : update) {
			map.put(clock.clock.id(), clock);
		}

		clocks = Map.copyOf(map);
	}

	@Override
	public void updateClockInstance(ResourceLocation id, int tick, boolean ticking) {
		var instance = clocks.get(id);

		if (instance != null) {
			instance.tick = instance.prevTick = tick;
			instance.ticking = ticking;
		}
	}

	@Override
	public void updateSessionData(Player self, UUID player, List<DataMapValue> playerData) {
		if (self.getUUID().equals(player)) {
			dataMap.update(self, playerData);
		} else {
			getRemoteSessionData(player).dataMap.update(self.level().getPlayerByUUID(player), playerData);
		}
	}

	@Override
	public void removeSessionData(UUID id) {
		remoteSessionData.remove(id);
	}

	@Override
	public void updatePlayerTags(UUID ownId, UUID uuid, List<String> update) {
		var t = ownId.equals(uuid) ? tags : getRemoteSessionData(uuid).tags;
		t.clear();
		t.addAll(update);
	}

	@Override
	public void updateServerData(List<DataMapValue> serverData) {
		serverDataMap.update(null, serverData);
	}

	@Override
	public void refreshBlockZones() {
		cachedZoneShapes = null;
	}
}
