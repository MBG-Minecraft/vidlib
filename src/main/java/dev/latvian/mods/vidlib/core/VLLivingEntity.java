package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBar;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

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

	default boolean isBoss() {
		return vl$self().getTags().contains("main_boss") || vl$self().hasCustomName() && vl$self().getCustomName().getString().equals("Boss");
	}

	@Nullable
	default ProgressBar getBossBar() {
		return isBoss() ? ProgressBar.DEFAULT_ENTITY : null;
	}
}
