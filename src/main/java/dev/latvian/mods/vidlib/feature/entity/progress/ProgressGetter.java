package dev.latvian.mods.vidlib.feature.entity.progress;

import dev.latvian.mods.vidlib.core.VLEntity;

@FunctionalInterface
public interface ProgressGetter {
	ProgressGetter ENTITY_HEALTH = (owner, delta) -> owner instanceof VLEntity entity ? entity.getRelativeHealth(delta) : 0F;

	float getProgress(Object owner, float delta);
}
