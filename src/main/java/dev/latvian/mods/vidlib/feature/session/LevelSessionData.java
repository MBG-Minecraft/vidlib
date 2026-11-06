package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.bulk.BlockModificationBuilder;
import dev.latvian.mods.klib.bulk.BulkLevelModification;
import dev.latvian.mods.klib.entity.EntityUtils;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.math.HitBuilder;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationHolder;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModification;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModificationHolder;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.zone.LevelZones;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LevelSessionData {
	public static LevelSessionData of(Level level) {
		return VLPlatformHelper.INSTANCE.getLevelData(level);
	}

	public final WorldSessionData worldData;
	public final ResourceKey<Level> dimension;
	public LevelZones zones;
	public Props<?> props;
	public final List<UndoableModificationHolder> undoable;

	public LevelSessionData(WorldSessionData worldData, ResourceKey<Level> dimension) {
		this.worldData = worldData;
		this.dimension = dimension;
		this.zones = null;
		this.props = null;
		this.undoable = new ArrayList<>();
	}

	public void tick(Level level, PauseType pauseType) {
		var globalNumberContext = new KNumberContext(worldData.globalVariables);
		globalNumberContext.updateLevelData(level);

		var allEntities = EntityUtils.getAllEntities(level);

		if (props != null) {
			props.tick(pauseType.tick());
		}

		if (zones != null) {
			zones.tick(level, allEntities);
		}
	}

	public boolean intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		if (props != null && props.levelProps.intersectsSolid(entity, collisionBox)) {
			return true;
		}

		if (zones != null && zones.intersectsSolid(entity, collisionBox)) {
			return true;
		}

		return false;
	}


	public void collectShapesIntersecting(@Nullable Entity entity, AABB collisionBox, List<VoxelShape> shapes) {
		if (props != null) {
			props.levelProps.collectShapesIntersecting(entity, collisionBox, shapes);
		}

		if (zones != null) {
			zones.collectShapesIntersecting(entity, collisionBox, shapes);
		}
	}

	@Nullable
	public FluidState getFluidState(Vec3i pos) {
		for (var c : zones.fluidZones) {
			if (c.fluid != null && c.volume.contains(pos)) {
				return c.fluid.fluidState();
			}
		}

		return null;
	}

	public float getFluidHeight(FlowingFluid fluid, Vec3i pos) {
		var above = getFluidState(pos.above());

		if (above != null && fluid.isSame(above.getType())) {
			return 1F;
		}

		return 0F;
	}

	public BlockHitResult clip(BlockHitResult result, ClipContext ctx) {
		var propClip = props.clip(ctx, false);

		if (propClip != null) {
			if (result == null || propClip.getLocation().distanceToSqr(ctx.getFrom()) < result.getLocation().distanceToSqr(ctx.getFrom())) {
				result = propClip;
			}
		}

		if (zones != null) {
			var zoneHit = zones.hitLevel(new HitBuilder(ctx.getFrom(), ctx.getTo()));

			if (zoneHit != null && zoneHit.distanceSq() < result.getLocation().distanceToSqr(ctx.getFrom())) {
				var r = zoneHit.asBlockHitResult();

				if (r != null) {
					result = r;
				}
			}
		}

		return result;
	}

	public void addUndoable(Level level, UndoableModification modification) {
		undoable.add(new UndoableModificationHolder(level.getGameTime(), modification));
	}

	public int undoLastModification(Level level) {
		if (!undoable.isEmpty()) {
			var builder = new BlockModificationBuilder();
			undoable.getLast().modification().undo(level, builder);
			undoable.removeLast();
			return level.bulkModify(false, builder.build());
		}

		return 0;
	}

	public int undoAllModifications(Level level) {
		var builder = new BlockModificationBuilder();

		for (int i = undoable.size() - 1; i >= 0; i--) {
			undoable.get(i).modification().undo(level, builder);
		}

		undoable.clear();
		return level.bulkModify(false, builder.build());
	}

	public int undoAllFutureModifications(Level level, boolean everything) {
		var builder = new BlockModificationBuilder();
		var gameTime = level.getGameTime();

		for (int i = undoable.size() - 1; i >= 0; i--) {
			var u = undoable.get(i);

			if (everything || u.gameTime() > gameTime) {
				u.modification().undo(level, builder);
				undoable.remove(i);
			}
		}

		var result = builder.build();

		if (result == BulkLevelModification.NONE) {
			return 0;
		}

		return new BulkLevelModificationHolder().apply(level, result.value(), false, true);
	}
}
