package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBar;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface VLLivingEntity extends VLEntity {
	@Override
	default LivingEntity vl$self() {
		return (LivingEntity) this;
	}

	default void heal() {
		CommonGameEngine.INSTANCE.heal(vl$self());
	}

	@Nullable
	default ProgressBar getBossBar() {
		return null;
	}
}
