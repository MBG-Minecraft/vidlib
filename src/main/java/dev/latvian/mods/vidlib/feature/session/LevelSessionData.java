package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.bulk.BlockModificationBuilder;
import dev.latvian.mods.klib.bulk.BulkLevelModification;
import dev.latvian.mods.klib.entity.EntityUtils;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.math.HitBuilder;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationHolder;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModification;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModificationHolder;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.InternalLevelData;
import dev.latvian.mods.vidlib.feature.environment.FluidPlane;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
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
import java.util.Collection;
import java.util.List;

public abstract class LevelSessionData<P extends Props<?>> {
	public static LevelSessionData<?> of(Level level) {
		return VLPlatformHelper.INSTANCE.getLevelData(level);
	}

	public final GlobalSessionData globalData;
	public final ResourceKey<Level> dimension;
	public final DataMap<LevelSessionData<?>> dataMap;
	public P props;
	public final LevelZones zones;
	public final List<UndoableModificationHolder> undoable;

	public LevelSessionData(GlobalSessionData globalData, ResourceKey<Level> dimension) {
		this.globalData = globalData;
		this.dimension = dimension;
		this.dataMap = new DataMap<>(globalData, this, DataKey.LEVEL);
		this.props = null;
		this.zones = new LevelZones(dimension);
		this.undoable = new ArrayList<>();
	}

	public KNumberContext createGlobalContext() {
		return new KNumberContext(globalData.globalVariables);
	}

	public void tick(Level level, PauseType pauseType) {
		var allEntities = EntityUtils.getAllEntities(level);

		if (props != null) {
			props.tick(pauseType.tick());
		}

		zones.tick(level, allEntities);
	}

	public boolean intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		if (props != null && props.levelProps.intersectsSolid(entity, collisionBox)) {
			return true;
		}

		if (zones.intersectsSolid(entity, collisionBox)) {
			return true;
		}

		return false;
	}


	public void collectShapesIntersecting(@Nullable Entity entity, AABB collisionBox, List<VoxelShape> shapes) {
		if (props != null) {
			props.levelProps.collectShapesIntersecting(entity, collisionBox, shapes);
		}

		zones.collectShapesIntersecting(entity, collisionBox, shapes);
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
		var propClip = props == null ? null : props.clip(ctx, false);

		if (propClip != null) {
			if (result == null || propClip.getLocation().distanceToSqr(ctx.getFrom()) < result.getLocation().distanceToSqr(ctx.getFrom())) {
				result = propClip;
			}
		}

		var zoneHit = zones.hitLevel(new HitBuilder(ctx.getFrom(), ctx.getTo()));

		if (zoneHit != null && zoneHit.distanceSq() < result.getLocation().distanceToSqr(ctx.getFrom())) {
			var r = zoneHit.asBlockHitResult();

			if (r != null) {
				result = r;
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

	@Nullable
	public Ref<Atmosphere> getAtmosphere() {
		return dataMap.getOptional(InternalLevelData.ATMOSPHERE);
	}

	public void setAtmosphere(@Nullable Ref<Atmosphere> value) {
		dataMap.set(InternalLevelData.ATMOSPHERE, value);
	}

	public Anchor getAnchor() {
		return dataMap.get(InternalLevelData.ANCHOR);
	}

	public void setAnchor(Anchor value) {
		dataMap.set(InternalLevelData.ANCHOR, value);
	}

	public List<Waypoint> getWaypoints() {
		return dataMap.get(InternalLevelData.WAYPOINTS);
	}

	public void setWaypoints(List<Waypoint> value) {
		dataMap.set(InternalLevelData.WAYPOINTS, List.copyOf(value));
	}

	public void addWaypoints(List<Waypoint> waypoints) {
		var list = new ArrayList<>(getWaypoints());
		list.addAll(waypoints);
		setWaypoints(list);
	}

	public void removeWaypoints(Collection<String> ids) {
		var list = new ArrayList<Waypoint>();

		for (var waypoint : getWaypoints()) {
			if (!ids.contains(waypoint.id())) {
				list.add(waypoint);
			}
		}

		setWaypoints(list);
	}

	@Nullable
	public FluidPlane getFluidPlane() {
		return dataMap.getOptional(InternalLevelData.FLUID_PLANE);
	}

	public void setFluidPlane(@Nullable FluidPlane value) {
		dataMap.set(InternalLevelData.FLUID_PLANE, value);
	}

	public boolean getIceMelts() {
		return dataMap.get(InternalLevelData.ICE_MELTS);
	}

	public void setIceMelts(boolean value) {
		dataMap.set(InternalLevelData.ICE_MELTS, value);
	}

	public boolean getBlockGravity() {
		return dataMap.get(InternalLevelData.BLOCK_GRAVITY);
	}

	public void setBlockGravity(boolean value) {
		dataMap.set(InternalLevelData.BLOCK_GRAVITY, value);
	}

	public boolean getSmallGrassHitbox() {
		return dataMap.get(InternalLevelData.SMALL_GRASS_HITBOX);
	}

	public void setSmallGrassHitbox(boolean value) {
		dataMap.set(InternalLevelData.SMALL_GRASS_HITBOX, value);
	}

	public boolean getSoftBarriers() {
		return dataMap.get(InternalLevelData.SOFT_BARRIERS);
	}

	public void setSoftBarriers(boolean value) {
		dataMap.set(InternalLevelData.SOFT_BARRIERS, value);
	}
}
