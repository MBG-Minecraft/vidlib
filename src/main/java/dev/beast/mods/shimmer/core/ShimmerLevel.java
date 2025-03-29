package dev.beast.mods.shimmer.core;

import com.mojang.datafixers.util.Either;
import dev.beast.mods.shimmer.feature.bulk.BlockModificationConsumer;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModification;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationBundle;
import dev.beast.mods.shimmer.feature.bulk.BulkLevelModificationHolder;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.bulk.UndoableModification;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.feature.particle.FireData;
import dev.beast.mods.shimmer.feature.particle.WindData;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleData;
import dev.beast.mods.shimmer.feature.prop.PropList;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.math.worldposition.EntityPositionType;
import dev.beast.mods.shimmer.math.worldposition.FollowingEntityWorldPosition;
import dev.beast.mods.shimmer.math.worldposition.WorldPosition;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface ShimmerLevel extends ShimmerEntityContainer, ShimmerMinecraftEnvironmentDataHolder {
	@Override
	default List<Entity> shimmer$getEntities() {
		return ((Level) this).getEntities((Entity) null, AABB.INFINITE, EntitySelector.ENTITY_STILL_ALIVE);
	}

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
	default List<? extends Player> shimmer$getPlayers() {
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
		shimmer$getUndoableModifications().add(modification);
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

	default void redrawSections(LongList sections, boolean mainThread) {
	}

	default void playSound(Vec3 pos, SoundData sound) {
	}

	default void playTrackingSound(WorldPosition position, WorldNumberVariables variables, SoundData data, boolean looping) {
	}

	default void playTrackingSound(Entity entity, SoundData data, boolean looping) {
		playTrackingSound(new FollowingEntityWorldPosition(Either.left(entity.getId()), EntityPositionType.EYES), WorldNumberVariables.EMPTY, data, looping);
	}

	default void physicsParticles(PhysicsParticleData data, long seed, List<PositionedBlock> blocks) {
	}

	default void physicsParticles(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
	}

	default void physicsParticles(PhysicsParticleData data, List<PositionedBlock> blocks) {
		physicsParticles(data, shimmer$level().shimmer$level().random.nextLong(), blocks);
	}

	default void physicsParticles(ResourceLocation id, List<PositionedBlock> blocks) {
		physicsParticles(id, shimmer$level().shimmer$level().random.nextLong(), blocks);
	}

	default void spawnCubeParticles(Map<CubeParticleOptions, List<BlockPos>> map) {
	}

	default void spawnCubeParticles(CubeParticleOptions options, List<BlockPos> blocks) {
		spawnCubeParticles(Map.of(options, blocks));
	}

	default void spawnWindParticles(RandomSource random, WindData data) {
	}

	default void spawnFireParticles(RandomSource random, FireData data) {
	}
}
