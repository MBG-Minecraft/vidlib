package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationBundle;
import dev.latvian.mods.vidlib.feature.bulk.OptimizedModificationBuilder;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.prop.ServerProps;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

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

	default void vl$setActiveZones(ActiveZones zones) {
	}

	@Override
	@Nullable
	default Entity getEntityByUUID(UUID uuid) {
		return this.vl$level().getEntity(uuid);
	}

	@Override
	default int bulkModify(boolean undoable, BulkLevelModification modification) {
		var optimized = modification.optimize();

		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		if (optimized instanceof BulkLevelModificationBundle(List<BulkLevelModification> list)) {
			var builder = new OptimizedModificationBuilder();

			for (var m : list) {
				m.apply(builder);
			}

			optimized = builder.build();
		}

		return VLLevel.super.bulkModify(undoable, optimized);
	}

	default void vl$reloadChunks() {
		throw new NoMixinException(this);
	}

	default void vl$updateLoadedChunks() {
	}

	default void vl$updateLoadedChunks(LongSet tickets) {
		var level = this.vl$level();
		var toLoad = new LongOpenHashSet();

		var activeZones = vl$getActiveZones();

		if (activeZones != null) {
			for (var container : activeZones) {
				for (var zone : container.zones) {
					if (zone.zone.forceLoaded()) {
						zone.zone.shape().collectChunkPositions(toLoad);
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

		var toUnload = new LongOpenHashSet(tickets);
		toUnload.removeAll(toLoad);

		toLoad.removeAll(tickets);

		VidLib.LOGGER.info("Loaded " + toLoad.size() + " chunks, unloaded " + toUnload.size() + " chunks");

		for (var pos : toLoad) {
			Anchor.TICKET_CONTROLLER.forceChunk(level, Util.NIL_UUID, ChunkPos.getX(pos), ChunkPos.getZ(pos), true, false);
		}

		for (var pos : toUnload) {
			Anchor.TICKET_CONTROLLER.forceChunk(level, Util.NIL_UUID, ChunkPos.getX(pos), ChunkPos.getZ(pos), false, false);
		}
	}

	@Override
	default void discardAll(EntityFilter filter) {
		for (var entity : this.vl$level().getAllEntities()) {
			if (filter.test(entity)) {
				entity.discard();
			}
		}
	}

	@Override
	default void killAll(EntityFilter filter) {
		for (var entity : this.vl$level().getAllEntities()) {
			if (filter.test(entity)) {
				entity.kill(this.vl$level());
			}
		}
	}

	@Override
	default boolean isReplayLevel() {
		return vl$level().getServer().getClass().getName().equals("com.moulberry.flashback.playback.ReplayServer");
	}

	@Override
	default Iterable<Entity> allEntities() {
		return vl$level().getEntities().getAll();
	}
}
