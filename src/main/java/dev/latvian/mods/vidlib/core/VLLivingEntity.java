package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBar;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public interface VLLivingEntity extends VLEntity {
	@Override
	default LivingEntity vl$self() {
		return (LivingEntity) this;
	}

	default void heal() {
		CommonGameEngine.INSTANCE.heal(vl$self());
	}

	@Override
	default float vl$getHealth(float delta) {
		return vl$self().getHealth();
	}

	@Override
	default float vl$getMaxHealth(float delta) {
		return vl$self().getMaxHealth();
	}

	default boolean isBoss() {
		return CommonGameEngine.INSTANCE.isBoss(vl$self());
	}

	@Nullable
	default ProgressBar getBossBar() {
		return isBoss() ? (this instanceof Player p ? ProgressBar.PLAYER.apply(p) : ProgressBar.DEFAULT_ENTITY) : null;
	}

	@Override
	default boolean vl$hasItem(Ingredient ingredient) {
		var entity = vl$self();

		for (var slot : EquipmentSlot.VALUES) {
			var stack = entity.getItemBySlot(slot);

			if (!stack.isEmpty() && ingredient.test(stack)) {
				return true;
			}
		}

		return false;
	}

	@Override
	default boolean vl$isDeadOrDying() {
		return vl$self().isDeadOrDying();
	}
}
