package dev.latvian.mods.vidlib.core;

import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public interface VLEntitySelector extends Predicate<Entity> {
	@Override
	default boolean test(Entity entity) {
		throw new NoMixinException(this);
	}
}
