package dev.latvian.mods.vidlib.core;

import net.minecraft.world.entity.LivingEntity;

public interface VLLivingEntity extends VLEntity {
	@Override
	default LivingEntity vl$self() {
		return (LivingEntity) this;
	}

	default void heal() {
		var entity = vl$self();

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
		var e = vl$self();
		return Math.clamp(e.getHealth() / e.getMaxHealth(), 0F, 1F);
	}
}
