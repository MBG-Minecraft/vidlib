package dev.latvian.mods.vidlib.feature.entity.filter;

import dev.latvian.mods.klib.registry.UnitType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public record BasicEntityFilter(UnitType<RegistryFriendlyByteBuf, EntityFilter> type, Predicate<Entity> predicate) implements EntityFilter {
	@Override
	public boolean test(Entity entity) {
		return predicate.test(entity);
	}

	@Override
	public String toString() {
		return type().key();
	}
}
