package dev.latvian.mods.vidlib.feature.entity.filter;

import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public record BasicEntityFilter(Predicate<Entity> predicate) implements EntityFilter {
	@Override
	public boolean test(Entity entity) {
		return predicate.test(entity);
	}

	@Override
	public String toString() {
		return type().id();
	}
}
