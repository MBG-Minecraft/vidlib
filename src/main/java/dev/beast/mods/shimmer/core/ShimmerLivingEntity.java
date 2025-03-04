package dev.beast.mods.shimmer.core;

import net.minecraft.world.entity.LivingEntity;

public interface ShimmerLivingEntity extends ShimmerEntity {
	default void heal() {
		var entity = (LivingEntity) this;

		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(entity.getMaxHealth());
		}

		entity.extinguishFire();
	}
}
