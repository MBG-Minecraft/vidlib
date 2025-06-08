package dev.latvian.mods.vidlib.core;

import com.google.gson.JsonElement;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
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
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import dev.latvian.mods.vidlib.util.PauseType;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
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

	@ApiStatus.Internal
	default void vl$preTick(PauseType paused) {
	}

	default long vl$nextPacketId() {
		throw new NoMixinException(this);
	}

	default Props<?> getProps() {
		throw new NoMixinException(this);
	}

	@Override
	default DataMap getServerData() {
		return getEnvironment().getServerData();
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

	default int undoAllFutureModifications() {
		var builder = new OptimizedModificationBuilder();
		var undoable = vl$getUndoableModifications();
		var gameTime = vl$level().getGameTime();

		for (int i = undoable.size() - 1; i >= 0; i--) {
			var u = undoable.get(i);

			if (u.gameTime() > gameTime) {
				u.modification().undo((Level) this, builder);
				undoable.remove(i);
			}
		}

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

	default void walkBlocks(ConnectedBlock.WalkType walkType, BlockPos start, @Nullable BlockFilter filter, int maxDistance, Predicate<ConnectedBlock> callback) {
		var traversed = new LongOpenHashSet();
		var queue = new ArrayDeque<ConnectedBlock>();

		queue.add(new ConnectedBlock(new PositionedBlock(start, vl$level().getBlockState(start)), 0));
		traversed.add(start.asLong());
		var level = vl$level();

		while (!queue.isEmpty()) {
			var c = queue.pop();

			if (c.distance() == 0 || (filter == null ? !c.block().state().isAir() : filter.test(level, c.block().pos(), c.block().state()))) {
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

	default List<ConnectedBlock> walkBlocks(ConnectedBlock.WalkType walkType, BlockPos start, @Nullable BlockFilter filter, int maxDistance, int maxTotalBlocks) {
		var result = new Long2ObjectOpenHashMap<ConnectedBlock>();

		walkBlocks(walkType, start, filter, maxDistance, c -> {
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
		var zones = vl$getActiveZones();
		var state = zones == null ? null : zones.getZoneFluidState(pos);
		return state == null ? vl$level().getFluidState(pos) : state;
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

	default boolean isBlockExposed(int x, int y, int z, BlockPos.MutableBlockPos mutablePos) {
		var level = vl$level();

		return !level.getBlockState(mutablePos.set(x, y + 1, z)).isVisible()
			|| !level.getBlockState(mutablePos.set(x, y - 1, z)).isVisible()
			|| !level.getBlockState(mutablePos.set(x - 1, y, z)).isVisible()
			|| !level.getBlockState(mutablePos.set(x + 1, y, z)).isVisible()
			|| !level.getBlockState(mutablePos.set(x, y, z - 1)).isVisible()
			|| !level.getBlockState(mutablePos.set(x, y, z + 1)).isVisible();
	}

	default WorldNumberContext globalContext(float progress) {
		return new WorldNumberContext(vl$level(), progress, getEnvironment().globalVariables());
	}

	default WorldNumberContext globalContext() {
		return globalContext(1F);
	}

	default RegistryOps<Tag> nbtOps() {
		return vl$level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}

	default RegistryOps<JsonElement> jsonOps() {
		return vl$level().registryAccess().createSerializationContext(JsonOps.INSTANCE);
	}
}
