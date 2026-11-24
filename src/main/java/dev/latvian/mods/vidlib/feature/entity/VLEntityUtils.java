package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.entity.LivingEntity;

public interface VLEntityUtils {
	static void heal(LivingEntity entity) {
		CommonGameEngine.INSTANCE.heal(entity);
	}
}
