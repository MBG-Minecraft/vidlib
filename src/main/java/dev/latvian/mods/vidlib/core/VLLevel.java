package dev.latvian.mods.vidlib.core;

import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.IntOrUUID;
import dev.latvian.mods.vidlib.feature.block.ConnectedBlock;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.bulk.BlockModificationConsumer;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationBundle;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationHolder;
import dev.latvian.mods.vidlib.feature.bulk.OptimizedModificationBuilder;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModification;
import dev.latvian.mods.vidlib.feature.bulk.UndoableModificationHolder;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityTypeFilter;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.util.PauseType;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface VLLevel extends VLPlayerContainer, VLMinecraftEnvironmentDataHolder {
	@Override
	default Level vl$level() {
		return (Level) this;
	}

	default boolean isServerSide() {
		return !getEnvironment().isClient();
	}

	@ApiStatus.Internal
	default void vl$preTick(PauseType paused) {
	}

	default Props<?> getProps() {
		throw new NoMixinException(this);
	}

	@Override
	default DataMap getServerData() {
		return getEnvironment().getServerData();
	}

	@Override
	default FeatureSet getServerFeatures() {
		return getEnvironment().getServerFeatures();
	}

	@Nullable
	default ActiveZones vl$getActiveZones() {
		throw new NoMixinException(this);
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
			var builder = new OptimizedModificationBuilder();
			undoable.getLast().modification().undo((Level) this, builder);
			undoable.removeLast();
			return bulkModify(false, builder.build());
		}

		return 0;
	}

	default int undoAllModifications() {
		var builder = new OptimizedModificationBuilder();
		var undoable = vl$getUndoableModifications();

		for (int i = undoable.size() - 1; i >= 0; i--) {
			undoable.get(i).modification().undo((Level) this, builder);
		}

		undoable.clear();
		return bulkModify(false, builder.build());
	}

	default int undoAllFutureModifications(boolean everything) {
		var builder = new OptimizedModificationBuilder();
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

		return new BulkLevelModificationHolder().apply((Level) this, result, false, true);
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
	default Entity getEntity(IntOrUUID id) {
		return id.getEntity(vl$level());
	}

	default int bulkModify(boolean undoable, BulkLevelModification modification) {
		if (modification == BulkLevelModification.NONE) {
			return 0;
		}

		return new BulkLevelModificationHolder().apply((Level) this, modification, undoable, false);
	}

	default int bulkModify(boolean undoable, Consumer<BlockModificationConsumer> modifications) {
		var m = new BulkLevelModificationBundle(new ArrayList<>());
		modifications.accept(m);
		return bulkModify(undoable, m);
	}

	default void walkBlocks(ConnectedBlock.WalkType walkType, BlockPos start, @Nullable BlockFilter filter, boolean onlyExposed, int maxDistance, Predicate<ConnectedBlock> callback) {
		if (filter == BlockFilter.ANY.instance()) {
			filter = null;
		}

		var traversed = new LongOpenHashSet();
		var queue = new ArrayDeque<ConnectedBlock>();
		var partialCache = new Long2IntOpenHashMap();
		partialCache.defaultReturnValue(-1);
		var partialMutablePos = new BlockPos.MutableBlockPos();

		queue.add(new ConnectedBlock(new PositionedBlock(start, vl$level().getBlockState(start)), 0));
		traversed.add(start.asLong());
		var level = vl$level();

		while (!queue.isEmpty()) {
			var c = queue.pop();

			if (c.distance() == 0 || (filter == null ? !c.block().state().isAir() : filter.test(level, c.block().pos(), c.block().state()))) {
				if (onlyExposed && !isBlockExposed(partialCache, c.block().pos().getX(), c.block().pos().getY(), c.block().pos().getZ(), partialMutablePos)) {
					continue;
				}

				if (callback.test(c)) {
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
	}

	default List<ConnectedBlock> walkBlocks(ConnectedBlock.WalkType walkType, BlockPos start, @Nullable BlockFilter filter, boolean onlyExposed, int maxDistance, int maxTotalBlocks) {
		var result = new Long2ObjectOpenHashMap<ConnectedBlock>();

		walkBlocks(walkType, start, filter, onlyExposed, maxDistance, c -> {
			result.put(c.block().pos().asLong(), c);
			return result.size() >= maxTotalBlocks;
		});

		return List.copyOf(result.values());
	}

	default void discardAll(EntityFilter filter) {
	}

	default void discardAll(EntityType<?> type) {
		discardAll(new EntityTypeFilter(type));
	}

	default void killAll(EntityFilter filter) {
	}

	default void killAll(EntityType<?> type) {
		killAll(new EntityTypeFilter(type));
	}

	default boolean isReplayLevel() {
		return false;
	}

	default Iterable<Entity> allEntities() {
		return vl$level().getEntities((Entity) null, AABB.INFINITE, Entity::isAlive);
	}

	default Iterable<LivingEntity> allLivingEntities() {
		var list = new ArrayList<LivingEntity>();

		for (var entity : allEntities()) {
			if (entity instanceof LivingEntity livingEntity) {
				list.add(livingEntity);
			}
		}

		return list;
	}

	default List<Entity> selectEntities(EntitySelector selector) {
		var list = new ArrayList<Entity>(1);

		for (var entity : allEntities()) {
			if (selector.test(entity)) {
				list.add(entity);
			}
		}

		return list;
	}

	default List<Entity> selectEntities(CommandContext<?> ctx, String name) {
		return selectEntities(ctx.getArgument(name, EntitySelector.class));
	}

	default List<Player> selectPlayers(EntitySelector selector) {
		var list = new ArrayList<Player>(1);

		for (var player : vl$level().players()) {
			if (selector.test(player)) {
				list.add(player);
			}
		}

		return list;
	}

	default List<Player> selectPlayers(CommandContext<?> ctx, String name) {
		return selectPlayers(ctx.getArgument(name, EntitySelector.class));
	}

	default List<LivingEntity> getDamageableEntities(@Nullable Entity ignoredEntity, AABB box) {
		return (List) vl$level().getEntities(ignoredEntity, box, e -> e instanceof LivingEntity && (!(e instanceof Player) || e.isSurvivalLike()));
	}

	default float vl$getDelta() {
		return 1F;
	}

	default List<LivingEntity> getBosses() {
		return List.of();
	}

	@Nullable
	default LivingEntity getMainBoss() {
		return null;
	}

	default FluidState vl$overrideFluidState(BlockPos pos) {
		var original = vl$level().getFluidState(pos);

		if (original.isEmpty()) {
			var zones = vl$getActiveZones();
			var state = zones == null ? null : zones.getZoneFluidState(pos);
			return state == null ? original : state;
		}

		return original;
	}

	default BlockState vl$overrideFluidStateBlock(BlockPos pos) {
		var original = vl$level().getBlockState(pos);

		if (original.isAir()) {
			var zones = vl$getActiveZones();
			var state = zones == null ? null : zones.getZoneFluidState(pos);
			return state == null ? original : state.createLegacyBlock();
		}

		return original;
	}

	default float vl$overrideFluidHeight(FluidState state, BlockPos pos) {
		if (state.getType() instanceof FlowingFluid flowing) {
			var zones = vl$getActiveZones();
			var height = zones == null ? 0F : zones.getZoneFluidHeight(flowing, pos);

			if (height > 0F) {
				return height;
			}
		}

		return state.getHeight(vl$level(), pos);
	}

	default boolean isBlockPartial(Long2IntOpenHashMap cache, BlockPos pos) {
		long key = pos.asLong();
		int exposed = cache.get(key);

		if (exposed == -1) {
			exposed = vl$level().getBlockState(pos).isPartial() ? 1 : 0;
			cache.put(key, exposed);
		}

		return exposed == 1;
	}

	default boolean isBlockExposed(Long2IntOpenHashMap cache, int x, int y, int z, BlockPos.MutableBlockPos mutable) {
		return isBlockPartial(cache, mutable.set(x, y + 1, z))
			|| isBlockPartial(cache, mutable.set(x, y - 1, z))
			|| isBlockPartial(cache, mutable.set(x - 1, y, z))
			|| isBlockPartial(cache, mutable.set(x + 1, y, z))
			|| isBlockPartial(cache, mutable.set(x, y, z - 1))
			|| isBlockPartial(cache, mutable.set(x, y, z + 1));
	}

	default boolean isBlockExposed(int x, int y, int z, BlockPos.MutableBlockPos mutablePos) {
		var level = vl$level();

		return level.getBlockState(mutablePos.set(x, y + 1, z)).isPartial()
			|| level.getBlockState(mutablePos.set(x, y - 1, z)).isPartial()
			|| level.getBlockState(mutablePos.set(x - 1, y, z)).isPartial()
			|| level.getBlockState(mutablePos.set(x + 1, y, z)).isPartial()
			|| level.getBlockState(mutablePos.set(x, y, z - 1)).isPartial()
			|| level.getBlockState(mutablePos.set(x, y, z + 1)).isPartial();
	}

	default KNumberContext getGlobalContext() {
		return new KNumberContext(vl$level());
	}

	default RegistryOps<Tag> nbtOps() {
		return vl$level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}

	default RegistryOps<JsonElement> jsonOps() {
		return vl$level().registryAccess().createSerializationContext(JsonOps.INSTANCE);
	}

	default TagParser<Tag> nbtParser() {
		return TagParser.create(nbtOps());
	}

	default boolean vl$intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		var props = getProps();

		if (props.levelProps.intersectsSolid(entity, collisionBox) || props.dataProps.intersectsSolid(entity, collisionBox)) {
			return true;
		}

		var zones = vl$getActiveZones();

		return zones != null && zones.intersectsSolid(entity, collisionBox);
	}

	default List<VoxelShape> vl$getShapesIntersecting(@Nullable Entity entity, AABB collisionBox) {
		var props = getProps();
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
		var props = getProps();
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

	default boolean vl$getTickDayTime() {
		return true;
	}

	default double getGroundY(double x, double y, double z) {
		var level = vl$level();
		var bpos = new BlockPos.MutableBlockPos(x, y + 0.001D, z);
		BlockState state;

		while ((state = level.getBlockState(bpos)).isAir()) {
			bpos.move(0, -1, 0);

			if (level.isOutsideBuildHeight(bpos.getY())) {
				return Double.NaN;
			}
		}

		return bpos.getY() + state.getCollisionShape(level, bpos).max(Direction.Axis.Y);
	}
}
