package dev.beast.mods.shimmer.feature.entity;

import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.world.entity.Entity;

public interface EntityOverrideValue<T> {
	static <T> EntityOverrideValue<T> fixed(T value) {
		if (value == Boolean.TRUE) {
			return Cast.to(TRUE);
		} else if (value == Boolean.FALSE) {
			return Cast.to(FALSE);
		}

		return entity -> value;
	}

	EntityOverrideValue<Boolean> TRUE = entity -> true;
	EntityOverrideValue<Boolean> FALSE = entity -> false;

	T get(Entity entity);
}
