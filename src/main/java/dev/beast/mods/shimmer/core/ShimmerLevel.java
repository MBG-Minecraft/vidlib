package dev.beast.mods.shimmer.core;

import com.mojang.datafixers.util.Either;
import dev.beast.mods.shimmer.feature.block.ConnectedBlock;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.bulk.BlockModificationConsumer;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationHolder;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.bulk.UndoableModification;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.prop.PropList;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface ShimmerLevel extends ShimmerPlayerContainer, ShimmerMinecraftEnvironmentDataHolder {
	@Override
	default Level shimmer$level() {
		return (Level) this;
	}

	default long shimmer$nextPacketId() {
		throw new NoMixinException(this);
	}

	default PropList<?> getProps() {
		throw new NoMixinException(this);
	}

	@Override
	default DataMap getServerData() {
		return shimmer$getEnvironment().getServerData();
	}

	@Nullable
	default ActiveZones shimmer$getActiveZones() {
		throw new NoMixinException(this);
	}

	default List<UndoableModification> shimmer$getUndoableModifications() {
		throw new NoMixinException(this);
	}

	default void addUndoable(UndoableModification modification) {
		if (!shimmer$isClient()) {
			shimmer$getUndoableModifications().add(modification);
		}
	}

	default int undoLastModification() {
		var undoable = shimmer$getUndoableModifications();

		if (!undoable.isEmpty()) {
			var builder = new OptimizedModificationBuilder();
			undoable.getLast().undo((Level) this, builder);
			undoable.removeLast();
			return bulkModify(false, builder.build());
		}

		return 0;
	}

	default int undoAllModifications() {
		var builder = new OptimizedModificationBuilder();
		var undoable = shimmer$getUndoableModifications();

		for (int i = undoable.size() - 1; i >= 0; i--) {
			undoable.get(i).undo((Level) this, builder);
		}

		undoable.clear();
		return bulkModify(false, builder.build());
	}

	default void setBlockFast(BlockPos pos, BlockState state) {
		((Level) this).setBlock(pos, state, Block.UPDATE_CLIENTS, 0);
	}

	default void setBlockFast(BlockPos pos, Block block) {
		setBlockFast(pos, block.defaultBlockState());
	}

	@Nullable
	default Entity getEntityByUUID(UUID uuid) {
		throw new NoMixinException(this);
	}

	@Nullable
	default Entity getEntityByEither(Either<Integer, UUID> id) {
		return id.map(((Level) this)::getEntity, this::getEntityByUUID);
	}

	default int bulkModify(boolean undoable, BulkLevelModification modification) {
		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		return new BulkLevelModificationHolder().apply((Level) this, modification, undoable);
	}

	default int bulkModify(boolean undoable, Consumer<BlockModificationConsumer> modifications) {
		var m = new BulkLevelModificationBundle(new ArrayList<>());
		modifications.accept(m);
		return bulkModify(undoable, m);
	}

	default List<ConnectedBlock> walkBlocks(ConnectedBlock.WalkType walkType, BlockPos start, BlockFilter filter, int maxDistance, int maxTotalBlocks) {
		var result = new Long2ObjectOpenHashMap<ConnectedBlock>();
		var traversed = new LongOpenHashSet();
		var queue = new ArrayDeque<ConnectedBlock>();

		queue.add(new ConnectedBlock(new PositionedBlock(start, shimmer$level().getBlockState(start)), 0));
		traversed.add(start.asLong());
		var level = shimmer$level();

		while (!queue.isEmpty()) {
			var c = queue.pop();

			if (c.distance() == 0 || filter.test(level, c.block().pos(), c.block().state())) {
				result.put(c.block().pos().asLong(), c);

				if (result.size() >= maxTotalBlocks) {
					break;
				}

				if (c.distance() + 1 > maxDistance) {
					continue;
				}

				for (var o : walkType.offsets) {
					var offset = c.block().pos().offset(o);
					var state = level.getBlockState(offset);

					if (!state.isAir() && traversed.add(offset.asLong())) {
						queue.add(new ConnectedBlock(new PositionedBlock(offset, state), c.distance() + 1));
					}
				}
			}
		}

		return List.copyOf(result.values());
	}
}
