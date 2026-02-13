package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBar;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
	default float getRelativeHealth(float delta) {
		var e = vl$self();
		return Math.clamp(e.getHealth() / e.getMaxHealth(), 0F, 1F);
	}

	default boolean isBoss() {
		return CommonGameEngine.INSTANCE.isBoss(vl$self());
	}

	@Nullable
	default ProgressBar getBossBar() {
		return isBoss() ? (this instanceof Player p ? ProgressBar.PLAYER.apply(p) : ProgressBar.DEFAULT_ENTITY) : null;
	}
}
