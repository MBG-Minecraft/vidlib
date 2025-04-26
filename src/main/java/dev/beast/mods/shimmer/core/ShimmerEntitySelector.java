package dev.beast.mods.shimmer.core;

import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public interface ShimmerEntitySelector extends Predicate<Entity> {
	@Override
	default boolean test(Entity entity) {
		throw new NoMixinException(this);
	}
}
