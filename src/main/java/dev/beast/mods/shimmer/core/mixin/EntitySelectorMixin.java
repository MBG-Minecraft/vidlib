package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerEntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin implements ShimmerEntitySelector {
	@Shadow
	@Final
	private List<Predicate<Entity>> contextFreePredicates;

	@Override
	public boolean test(Entity entity) {
		if (entity == null || !entity.isAlive()) {
			return false;
		}

		for (var predicate : contextFreePredicates) {
			if (!predicate.test(entity)) {
				return false;
			}
		}

		return true;
	}
}
