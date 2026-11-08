package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLTicketTracker;
import dev.latvian.mods.vidlib.feature.prop.ServerProps;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TicketStorage;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

import java.util.UUID;

public class ServerLevelSessionData extends LevelSessionData<ServerProps> {
	public static ServerLevelSessionData of(Level level) {
		return (ServerLevelSessionData) LevelSessionData.of(level);
	}

	public final ServerLevel level;

	public ServerLevelSessionData(ServerSessionData worldData, ResourceKey<Level> dimension, ServerLevel level) {
		super(worldData, dimension);
		this.level = level;
	}

	@Override
	public KNumberContext createGlobalContext() {
		var ctx = new KNumberContext(globalData.globalVariables);
		ctx.updateLevelData(level);
		return ctx;
	}

	@Override
	public void setAnchor(Anchor anchor) {
		super.setAnchor(anchor);
		updateLoadedChunks();
	}

	public LongOpenHashSet getChunksToLoad() {
		var toLoad = new LongOpenHashSet();

		if (zones != null) {
			for (var zone : zones) {
				if (zone.anchor) {
					zone.volume.collectChunkPositions(toLoad);
				}
			}
		}

		for (var area : getAnchor().volumes()) {
			area.collectChunkPositions(toLoad);
		}

		return toLoad;
	}

	public void updateLoadedChunks() {
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

		var toLoad = getChunksToLoad();
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

	public void validateLoadedChunks(TicketHelper ticketHelper) {
		var toLoad = getChunksToLoad();
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
}
