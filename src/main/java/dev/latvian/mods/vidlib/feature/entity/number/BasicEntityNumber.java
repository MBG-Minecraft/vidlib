package dev.latvian.mods.vidlib.feature.entity.number;

import dev.latvian.mods.klib.registry.CustomRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.function.ToDoubleFunction;

public record BasicEntityNumber(CustomRegistryType<RegistryFriendlyByteBuf, EntityNumber> type, ToDoubleFunction<Entity> function) implements EntityNumber {
	@Override
	public double applyAsDouble(Entity entity) {
		return function.applyAsDouble(entity);
	}

	@Override
	public String toString() {
		return type().toString();
	}
}
