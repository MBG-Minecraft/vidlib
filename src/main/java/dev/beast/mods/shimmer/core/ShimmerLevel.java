package dev.beast.mods.shimmer.core;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ShimmerLevel extends ShimmerEntityContainer {
	@Override
	default List<Entity> shimmer$getEntities() {
		return ((Level) this).getEntities((Entity) null, AABB.INFINITE, EntitySelector.ENTITY_STILL_ALIVE);
	}

	@Override
	default List<? extends Player> shimmer$getPlayers() {
		return ((Level) this).players();
	}

	default void setFakeBlock(BlockPos pos, BlockState state) {
		((Level) this).setBlock(pos, state, 0, 0);
	}

	@Nullable
	default Entity getEntityByUUID(UUID uuid) {
		return null;
	}

	@Nullable
	default Entity getEntityByEither(Either<Integer, UUID> id) {
		return id.map(((Level) this)::getEntity, this::getEntityByUUID);
	}
}
