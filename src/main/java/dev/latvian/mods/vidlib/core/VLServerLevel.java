package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationBundle;
import dev.latvian.mods.vidlib.feature.bulk.OptimizedModificationBuilder;
import dev.latvian.mods.vidlib.feature.prop.ServerProps;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.ZoneCache;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

import java.util.List;
import java.util.UUID;

public interface VLServerLevel extends VLLevel {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return this.vl$level().getServer();
	}

	@Override
	default ServerLevel vl$level() {
		return (ServerLevel) this;
	}

	@Override
	default List<? extends Player> vl$getS2CPlayers() {
		return this.vl$level().players();
	}

	@Override
	default ServerProps getProps() {
		throw new NoMixinException(this);
	}

	default void vl$setActiveZones(ZoneCache zones) {
	}

	@Override
	default int bulkModify(boolean undoable, BulkLevelModification modification) {
		var optimized = modification.optimize();

		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		if (optimized instanceof BulkLevelModificationBundle(List<Ref<BulkLevelModification>> list)) {
			var builder = new OptimizedModificationBuilder();

			for (var m : list) {
				m.value().apply(builder);
			}

			optimized = builder.build();
		}

		return VLLevel.super.bulkModify(undoable, optimized);
	}

	default void vl$reloadChunks() {
		throw new NoMixinException(this);
	}

	default LongOpenHashSet vl$getChunksToLoad() {
		var level = vl$level();
		var toLoad = new LongOpenHashSet();

		var activeZones = vl$getActiveZones();

		if (activeZones != null) {
			for (var container : activeZones) {
				for (var zone : container.zones) {
					if (zone.volume.forceLoaded()) {
						zone.volume.shape().value().collectChunkPositions(toLoad);
					}
				}
			}
		}

		var anchored = getAnchor().shapes().get(level.dimension());

		if (anchored != null) {
			for (var area : anchored) {
				area.collectChunkPositions(toLoad);
			}
		}

		return toLoad;
	}

	default void vl$updateLoadedChunks() {
		var level = vl$level();
		var ticketStorage = level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
		var currentlyLoaded = new LongOpenHashSet();

		for (var entry : ((VLTicketTracker<UUID>) ticketStorage.getEntityForcedChunks()).vl$getTickets().long2ObjectEntrySet()) {
			for (var ticketOwner : entry.getValue()) {
				if (ticketOwner.vl$getId().equals(Anchor.TICKET_CONTROLLER.id())) {
					currentlyLoaded.add(entry.getLongKey());
					break;
				}
			}
		}

		var toLoad = vl$getChunksToLoad();
		int toLoadCount = toLoad.size();
		var toUnload = new LongOpenHashSet(currentlyLoaded);
		toUnload.removeAll(toLoad);
		toLoad.removeAll(currentlyLoaded);

		VidLib.LOGGER.info("Force-loaded " + toLoad.size() + "/" + toLoadCount + " chunks, unloaded " + toUnload.size() + " chunks (previously " + currentlyLoaded.size() + ")");

		for (var pos : toLoad) {
			Anchor.TICKET_CONTROLLER.forceChunk(level, Util.NIL_UUID, ChunkPos.getX(pos), ChunkPos.getZ(pos), true, false);
		}

		for (var pos : toUnload) {
			Anchor.TICKET_CONTROLLER.forceChunk(level, Util.NIL_UUID, ChunkPos.getX(pos), ChunkPos.getZ(pos), false, false);
		}
	}

	default void vl$validateLoadedChunks(TicketHelper ticketHelper) {
		var toLoad = vl$getChunksToLoad();
		int unloaded = 0;
		int total = 0;
		var ticketSet = ticketHelper.getEntityTickets().get(Util.NIL_UUID);

		if (ticketSet != null) {
			var itr = new LongArraySet(ticketSet.normal()).iterator();

			while (itr.hasNext()) {
				var chunk = itr.nextLong();

				if (!toLoad.contains(chunk)) {
					ticketHelper.removeTicket(Util.NIL_UUID, chunk, false);
					unloaded++;
				}

				total++;
			}
		}

		VidLib.LOGGER.info("Unloaded " + unloaded + "/" + total + " expired force-loaded chunks");
	}

	@Override
	default void vl$setDayTime(long time) {
		vl$level().dimensionType().defaultClock().ifPresent(clock -> vl$level().clockManager().setTotalTicks(clock, time));
	}
}
