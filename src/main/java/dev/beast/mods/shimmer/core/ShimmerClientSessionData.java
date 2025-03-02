package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.ZoneClipResult;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import dev.beast.mods.shimmer.util.Side;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collection;

public class ShimmerClientSessionData extends ShimmerSessionData {
	public final ActiveZones serverZones;
	public final ActiveZones filteredZones;
	public ZoneClipResult zoneClip;

	public ShimmerClientSessionData() {
		Shimmer.LOGGER.info("Client Session Data Initialized");
		this.serverZones = new ActiveZones();
		this.filteredZones = new ActiveZones();
	}

	public void refreshZones(ResourceKey<Level> dimension) {
		filteredZones.filter(dimension, serverZones);
		NeoForge.EVENT_BUS.post(new ZoneEvent.Updated(dimension, filteredZones, Side.CLIENT));
	}

	@Override
	public void updateZones(Level level, Collection<ZoneContainer> update) {
		serverZones.update(update);
		NeoForge.EVENT_BUS.post(new ZoneEvent.AllUpdated(serverZones, Side.SERVER));
		refreshZones(level.dimension());
	}

	@Override
	public void respawned(Level level) {
		refreshZones(level.dimension());
	}

	@Override
	public void closed() {
	}
}
