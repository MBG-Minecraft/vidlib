package dev.beast.mods.shimmer.core;

import com.mojang.datafixers.util.Either;
import dev.beast.mods.shimmer.feature.bulk.BlockModificationConsumer;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationHolder;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.bulk.UndoableModification;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface ShimmerLevel extends ShimmerEntityContainer {
	@Override
	default List<Entity> shimmer$getEntities() {
		return ((Level) this).getEntities((Entity) null, AABB.INFINITE, EntitySelector.ENTITY_STILL_ALIVE);
	}

	default DataMap getServerData() {
		return shimmer$getEnvironment().getServerData();
	}

	@Override
	default List<? extends Player> shimmer$getPlayers() {
		return ((Level) this).players();
	}

	@Nullable
	default ActiveZones shimmer$getActiveZones() {
		throw new NoMixinException();
	}

	default List<UndoableModification> shimmer$getUndoableModifications() {
		throw new NoMixinException();
	}

	default void addUndoable(UndoableModification modification) {
		shimmer$getUndoableModifications().add(modification);
	}

	default int undoLastModification() {
		var undoable = shimmer$getUndoableModifications();

		if (!undoable.isEmpty()) {
			var builder = new OptimizedModificationBuilder();
			undoable.getLast().undo((Level) this, builder);
			undoable.removeLast();
			return bulkModify(builder.build());
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
		return bulkModify(builder.build());
	}

	default void setFakeBlock(BlockPos pos, BlockState state) {
		((Level) this).setBlock(pos, state, 0, 0);
	}

	default void setBlockFast(BlockPos pos, BlockState state) {
		((Level) this).setBlock(pos, state, Block.UPDATE_CLIENTS, 0);
	}

	default void setBlockFast(BlockPos pos, Block block) {
		setBlockFast(pos, block.defaultBlockState());
	}

	@Nullable
	default Entity getEntityByUUID(UUID uuid) {
		return null;
	}

	@Nullable
	default Entity getEntityByEither(Either<Integer, UUID> id) {
		return id.map(((Level) this)::getEntity, this::getEntityByUUID);
	}

	default int bulkModify(BulkLevelModification modification) {
		return new BulkLevelModificationHolder().apply((Level) this, modification);
	}

	default int bulkModify(Consumer<BlockModificationConsumer> modifications) {
		var m = new BulkLevelModificationBundle(new ArrayList<>());
		modifications.accept(m);
		return bulkModify(m);
	}

	default void redrawSection(int sectionX, int sectionY, int sectionZ, boolean mainThread) {
	}

	default void playTrackingSound(Entity entity, SoundData data, boolean looping) {
	}
}
