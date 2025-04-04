package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.prop.ServerPropList;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.feature.zone.Anchor;
import dev.beast.mods.shimmer.feature.zone.Zone;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
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

	default void shimmer$updateLoadedChunks(List<Ticket<ChunkPos>> tickets) {
		var level = this.shimmer$level();

		if (!tickets.isEmpty()) {
			for (var ticket : tickets) {
				((ShimmerDistanceManager) level.getChunkSource().distanceManager).shimmer$setLoaded(ticket, false);
			}

			Shimmer.LOGGER.info("Unloaded " + tickets.size() + " tickets");
			tickets.clear();
		}

		var activeZones = shimmer$getActiveZones();

		if (activeZones != null) {
			var loaded = new LongOpenHashSet();

			for (var container : activeZones) {
				for (var zone : container.zones) {
					if (zone.zone.forceLoaded()) {
						zone.zone.shape().collectChunkPositions(loaded);
					}
				}
			}

			if (!loaded.isEmpty()) {
				Shimmer.LOGGER.info(loaded.size() + " zone loaded chunks");
			}

			for (var pos : loaded) {
				var chunkPos = new ChunkPos(pos);
				tickets.add(new Ticket<>(Zone.TICKET_TYPE, ChunkMap.FORCED_TICKET_LEVEL, chunkPos, false));
			}
		}

		var anchored = getAnchor().shapes().get(level.dimension());

		if (anchored != null) {
			var loaded = new LongOpenHashSet();

			for (var area : anchored) {
				area.collectChunkPositions(loaded);
			}

			if (!loaded.isEmpty()) {
				Shimmer.LOGGER.info(loaded.size() + " anchored chunks");
			}

			for (var pos : loaded) {
				var chunkPos = new ChunkPos(pos);
				tickets.add(new Ticket<>(Anchor.TICKET_TYPE, ChunkMap.FORCED_TICKET_LEVEL, chunkPos, false));
			}
		}

		if (!tickets.isEmpty()) {
			for (var ticket : tickets) {
				((ShimmerDistanceManager) level.getChunkSource().distanceManager).shimmer$setLoaded(ticket, true);
			}

			Shimmer.LOGGER.info("Loaded " + tickets.size() + " tickets");
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
