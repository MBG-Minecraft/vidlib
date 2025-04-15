package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.prop.ServerPropList;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.Anchor;
import dev.latvian.mods.vidlib.VidLib;
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

public interface ShimmerServerLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment getEnvironment() {
		return this.shimmer$level().getServer();
	}

	@Override
	default ServerLevel shimmer$level() {
		return (ServerLevel) this;
	}

	@Override
	default List<? extends Player> shimmer$getS2CPlayers() {
		return this.shimmer$level().players();
	}

	@Override
	default ServerPropList getProps() {
		throw new NoMixinException(this);
	}

	default void shimmer$setActiveZones(ActiveZones zones) {
	}

	@Override
	@Nullable
	default Entity getEntityByUUID(UUID uuid) {
		return this.shimmer$level().getEntity(uuid);
	}

	@Override
	default int bulkModify(boolean undoable, BulkLevelModification modification) {
		var optimized = modification.optimize();

		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		if (optimized instanceof BulkLevelModificationBundle bundle) {
			var builder = new OptimizedModificationBuilder();

			for (var m : bundle.list()) {
				m.apply(builder);
			}

			optimized = builder.build();
		}

		return ShimmerLevel.super.bulkModify(undoable, optimized);
	}

	default void shimmer$reloadChunks() {
		throw new NoMixinException(this);
	}

	default void shimmer$updateLoadedChunks() {
	}

	default void shimmer$updateLoadedChunks(LongSet tickets) {
		var level = this.shimmer$level();
		var toLoad = new LongOpenHashSet();

		var activeZones = shimmer$getActiveZones();

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
		for (var entity : this.shimmer$level().getAllEntities()) {
			if (filter.test(entity)) {
				entity.discard();
			}
		}
	}

	@Override
	default void killAll(EntityFilter filter) {
		for (var entity : this.shimmer$level().getAllEntities()) {
			if (filter.test(entity)) {
				entity.kill(this.shimmer$level());
			}
		}
	}
}
