package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ShimmerEntity extends ShimmerEntityContainer {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return ((Entity) this).level().shimmer$getEnvironment();
	}

	default <T> T shimmer$getDirectOverride(EntityOverride<T> override) {
		throw new NoMixinException();
	}

	default <T> void shimmer$setDirectOverride(EntityOverride<T> override, @Nullable EntityOverrideValue<T> value) {
	}

	default boolean shimmer$isSaving() {
		return false;
	}

	@Override
	default List<Entity> shimmer$getEntities() {
		return List.of((Entity) this);
	}

	default List<ZoneInstance> getZones() {
		return shimmer$getEnvironment().shimmer$getActiveZones().entityZones.getOrDefault(((Entity) this).getId(), List.of());
	}
}
