package dev.beast.mods.shimmer.core;

import com.mojang.datafixers.util.Either;
import dev.beast.mods.shimmer.feature.bulk.BlockModificationConsumer;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationHolder;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.bulk.UndoableModification;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.prop.PropList;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface ShimmerLevel extends ShimmerEntityContainer, ShimmerMinecraftEnvironmentDataHolder {
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

	@Override
	default List<? extends Player> shimmer$getS2CPlayers() {
		return ((Level) this).players();
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
}
