package dev.beast.mods.shimmer.feature.entity;

import net.minecraft.world.entity.Entity;

public interface EntityOverrideValue<T> {
	static <T> EntityOverrideValue<T> fixed(T value) {
		return entity -> value;
	}

	EntityOverrideValue<Boolean> TRUE = fixed(true);
	EntityOverrideValue<Boolean> FALSE = fixed(false);

	T get(Entity entity);
}
