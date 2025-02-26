package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import dev.beast.mods.shimmer.util.EntityContainer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ShimmerEntity extends EntityContainer {
	<T> T shimmer$getDirectOverride(EntityOverride<T> override);

	<T> void shimmer$setDirectOverride(EntityOverride<T> override, @Nullable EntityOverrideValue<T> value);

	boolean shimmer$isSaving();

	@Override
	default List<Entity> shimmer$getEntities() {
		return List.of((Entity) this);
	}
}
