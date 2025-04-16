package dev.latvian.mods.vidlib.core;

import net.minecraft.world.entity.LivingEntity;

public interface VLLivingEntity extends VLEntity {
	default void heal() {
		var entity = (LivingEntity) this;

		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(entity.getMaxHealth());
		}

		entity.extinguishFire();
	}

	default boolean vl$unpushable() {
		return false;
	}

	@Override
	default float getRelativeHealth(float delta) {
		var e = (LivingEntity) this;
		return Math.clamp(e.getHealth() / e.getMaxHealth(), 0F, 1F);
	}
}
