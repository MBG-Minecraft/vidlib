package dev.beast.mods.shimmer.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public interface ShimmerLevel extends ShimmerEntityContainer {
	@Override
	default List<Entity> shimmer$getEntities() {
		return ((Level) this).getEntities((Entity) null, AABB.INFINITE, EntitySelector.ENTITY_STILL_ALIVE);
	}

	@Override
	default List<? extends Player> shimmer$getPlayers() {
		return ((Level) this).players();
	}
}
