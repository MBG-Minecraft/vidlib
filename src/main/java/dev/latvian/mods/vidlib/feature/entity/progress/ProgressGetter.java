package dev.latvian.mods.vidlib.feature.entity.progress;

import dev.latvian.mods.klib.entity.EntityUtils;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface ProgressGetter {
	ProgressGetter ENTITY_HEALTH = (owner, delta) -> owner instanceof Entity entity ? EntityUtils.getRelativeHealth(entity) : 0F;

	float getProgress(Object owner, float delta);
}
