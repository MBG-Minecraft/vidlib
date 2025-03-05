package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.clock.SyncClockFontsPayload;
import dev.beast.mods.shimmer.feature.clock.SyncClockInstancePayload;
import dev.beast.mods.shimmer.feature.clock.SyncClocksPayload;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.SyncZonesPayload;
import dev.beast.mods.shimmer.feature.zone.ZoneClipResult;
import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import dev.beast.mods.shimmer.util.Side;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShimmerLocalClientSessionData extends ShimmerClientSessionData {
	public final ActiveZones serverZones;
	public final ActiveZones filteredZones;
	private final Map<UUID, ShimmerRemoteClientSessionData> remoteSessionData;
	public ZoneClipResult zoneClip;
	public Map<ResourceLocation, ClockFont> clockFonts;
	public Map<ResourceLocation, ClockInstance> clocks;

	public ShimmerLocalClientSessionData(UUID uuid) {
		super(uuid);
		Shimmer.LOGGER.info("Client Session Data Initialized");
		this.serverZones = new ActiveZones();
		this.filteredZones = new ActiveZones();
		this.remoteSessionData = new HashMap<>();
		this.clockFonts = Map.of();
		this.clocks = Map.of();
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
		NeoForge.EVENT_BUS.post(new ZoneEvent.Updated(dimension, filteredZones, Side.CLIENT));
	}

	@Override
	public void updateZones(Level level, SyncZonesPayload payload) {
		serverZones.update(payload.update());
		NeoForge.EVENT_BUS.post(new ZoneEvent.AllUpdated(serverZones, Side.SERVER));
		refreshZones(level.dimension());
	}

	@Override
	public void updateClockFonts(SyncClockFontsPayload payload) {
		var map = new HashMap<ResourceLocation, ClockFont>();

		for (var font : payload.update()) {
			map.put(font.id(), font);
		}

		clockFonts = Map.copyOf(map);
		ClockFont.CLIENT_SUPPLIER = () -> clockFonts;
	}

	@Override
	public void updateClocks(Level level, SyncClocksPayload payload) {
		var map = new HashMap<ResourceLocation, ClockInstance>();

		for (var clock : payload.update()) {
			map.put(clock.clock.id(), clock);
		}

		clocks = Map.copyOf(map);
	}

	@Override
	public void updateClockInstance(SyncClockInstancePayload payload) {
		var instance = clocks.get(payload.id());

		if (instance != null) {
			instance.tick = instance.prevTick = payload.tick();
			instance.ticking = payload.ticking();
		}
	}

	@Override
	public void updateSessionData(UUID ownId, SyncPlayerDataPayload payload) {
		if (ownId.equals(payload.player())) {
			updateSessionData(payload.playerData());
		} else {
			getRemoteSessionData(payload.player()).updateSessionData(payload.playerData());
		}
	}

	@Override
	public void removeSessionData(UUID id) {
		remoteSessionData.remove(id);
	}
}
