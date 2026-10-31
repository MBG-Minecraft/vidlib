package dev.latvian.mods.vidlib.core;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.bulk.BlockModificationBuilder;
import dev.latvian.mods.klib.bulk.BlockModificationConsumer;
import dev.latvian.mods.klib.bulk.BulkLevelModification;
import dev.latvian.mods.klib.bulk.BulkLevelModificationBundle;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationHolder;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModification;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModificationHolder;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.prop.Props;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface VLLevel extends VLPlayerContainer, VLMinecraftEnvironmentDataHolder {
	@Override
	default Level vl$level() {
		return (Level) this;
	}

	@ApiStatus.Internal
	default void vl$preTick(PauseType paused) {
	}

	default Props<?> getProps() {
		throw new NoMixinException(this);
	}

	@Override
	default DataMap getDataMap() {
		return getEnvironment().getDataMap();
	}

	@Override
	default FeatureSet getServerFeatures() {
		return getEnvironment().getServerFeatures();
	}

	default List<UndoableModificationHolder> vl$getUndoableModifications() {
		throw new NoMixinException(this);
	}

	default void addUndoable(UndoableModification modification) {
		vl$getUndoableModifications().add(new UndoableModificationHolder(vl$level().getGameTime(), modification));
	}

	default int undoLastModification() {
		var undoable = vl$getUndoableModifications();

		if (!undoable.isEmpty()) {
			var builder = new BlockModificationBuilder();
			undoable.getLast().modification().undo((Level) this, builder);
			undoable.removeLast();
			return bulkModify(false, builder.build());
		}

		return 0;
	}

	default int undoAllModifications() {
		var builder = new BlockModificationBuilder();
		var undoable = vl$getUndoableModifications();

		for (int i = undoable.size() - 1; i >= 0; i--) {
			undoable.get(i).modification().undo((Level) this, builder);
		}

		undoable.clear();
		return bulkModify(false, builder.build());
	}

	default int undoAllFutureModifications(boolean everything) {
		var builder = new BlockModificationBuilder();
		var undoable = vl$getUndoableModifications();
		var gameTime = vl$level().getGameTime();

		for (int i = undoable.size() - 1; i >= 0; i--) {
			var u = undoable.get(i);

			if (everything || u.gameTime() > gameTime) {
				u.modification().undo((Level) this, builder);
				undoable.remove(i);
			}
		}

		var result = builder.build();

		if (result == BulkLevelModification.NONE) {
			return 0;
		}

		return new BulkLevelModificationHolder().apply((Level) this, result.value(), false, true);
	}

	default int bulkModify(boolean undoable, Ref<BulkLevelModification> modification) {
		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		return new BulkLevelModificationHolder().apply((Level) this, modification.value(), undoable, false);
	}

	default int bulkModify(boolean undoable, Consumer<BlockModificationConsumer> modifications) {
		var m = new BulkLevelModificationBundle(new ArrayList<>());
		modifications.accept(m);
		return bulkModify(undoable, m.ref());
	}

	default boolean vl$isReplayLevel() {
		return false;
	}

	default KNumberContext getGlobalContext() {
		var level = vl$level();
		var ctx = new KNumberContext(level.getEnvironment().globalVariables());
		ctx.updateLevelData(level);
		return ctx;
	}

	@Override
	default RegistryOps<Tag> nbtOps() {
		return vl$level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}

	@Override
	default RegistryOps<JsonElement> jsonOps() {
		return vl$level().registryAccess().createSerializationContext(JsonOps.INSTANCE);
	}

	default TagParser<Tag> nbtParser() {
		return TagParser.create(nbtOps());
	}

	default boolean vl$intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		var props = VLPlatformHelper.CURRENT.getProps(vl$level());

		if (props.levelProps.intersectsSolid(entity, collisionBox) || props.dataProps.intersectsSolid(entity, collisionBox)) {
			return true;
		}

		var zones = vl$getActiveZones();

		return zones != null && zones.intersectsSolid(entity, collisionBox);
	}

	default List<VoxelShape> vl$getShapesIntersecting(@Nullable Entity entity, AABB collisionBox) {
		var props = VLPlatformHelper.CURRENT.getProps(vl$level());
		var shapes = List.<VoxelShape>of();

		for (var propList : props.propLists.values()) {
			var intersecting = propList.getShapesIntersecting(entity, collisionBox);

			if (!intersecting.isEmpty()) {
				if (shapes.isEmpty()) {
					shapes = new ArrayList<>(intersecting.size());
				}

				shapes.addAll(intersecting);
			}
		}

		var zones = vl$getActiveZones();

		if (zones != null) {
			var list = zones.getShapesIntersecting(entity, collisionBox);

			if (!list.isEmpty()) {
				if (shapes.isEmpty()) {
					shapes = new ArrayList<>(list.size());
				}

				shapes.addAll(list);
			}
		}

		return shapes;
	}

	default BlockHitResult vl$clip(BlockHitResult result, ClipContext ctx) {
		var props = VLPlatformHelper.CURRENT.getProps(vl$level());
		var propClip = props.clip(ctx, false);

		if (propClip != null) {
			if (result == null || propClip.getLocation().distanceToSqr(ctx.getFrom()) < result.getLocation().distanceToSqr(ctx.getFrom())) {
				result = propClip;
			}
		}

		var zones = vl$getActiveZones();

		if (zones != null) {
			var zoneClip = zones.clipLevel(ctx);

			if (zoneClip != null && zoneClip.distanceSq() < result.getLocation().distanceToSqr(ctx.getFrom())) {
				var r = zoneClip.asBlockHitResult();

				if (r != null) {
					result = r;
				}
			}
		}

		return result;
	}
}
