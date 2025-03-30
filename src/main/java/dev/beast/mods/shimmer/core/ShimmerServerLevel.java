package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.prop.ServerPropList;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
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
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return shimmer$level().getServer();
	}

	@Override
	default ServerLevel shimmer$level() {
		return (ServerLevel) this;
	}

	@Override
	default List<? extends Player> shimmer$getS2CPlayers() {
		return shimmer$level().players();
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
		return shimmer$level().getEntity(uuid);
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

	default boolean shimmer$cancelWrite() {
		throw new NoMixinException(this);
	}

	default void shimmer$reloadChunks() {
		throw new NoMixinException(this);
	}

	default void shimmer$updateLoadedChunks(List<Ticket<ChunkPos>> tickets) {
		var level = shimmer$level();

		if (!tickets.isEmpty()) {
			for (var ticket : tickets) {
				((ShimmerDistanceManager) level.getChunkSource().distanceManager).shimmer$setLoaded(ticket, false);
			}

			Shimmer.LOGGER.info("Unloaded " + tickets.size() + " chunks");
			tickets.clear();
		}

		var loadedChunks = new LongOpenHashSet();

		for (var container : shimmer$getActiveZones()) {
			for (var zone : container.zones) {
				if (zone.zone.forceLoaded()) {
					zone.zone.shape().collectChunkPositions(loadedChunks);
				}
			}
		}

		if (!loadedChunks.isEmpty()) {
			for (var pos : loadedChunks) {
				var chunkPos = new ChunkPos(pos);
				var ticket = new Ticket<>(Zone.TICKET_TYPE, ChunkMap.FORCED_TICKET_LEVEL, chunkPos, false);
				((ShimmerDistanceManager) level.getChunkSource().distanceManager).shimmer$setLoaded(ticket, true);
				tickets.add(ticket);
			}

			Shimmer.LOGGER.info("Loaded " + loadedChunks.size() + " chunks");
		}
	}
}
