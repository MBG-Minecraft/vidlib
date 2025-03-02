package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
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
		var zones = ((Entity) this).level().shimmer$getActiveZones();
		return zones == null ? List.of() : zones.entityZones.getOrDefault(((Entity) this).getId(), List.of());
	}

	@Nullable
	default GameType getGameMode() {
		return null;
	}

	default boolean isSpectatorOrCreative() {
		var type = getGameMode();
		return type == GameType.SPECTATOR || type == GameType.CREATIVE;
	}

	default boolean isSurvival() {
		return getGameMode() == GameType.SURVIVAL;
	}

	default boolean isSurvivalLike() {
		var type = getGameMode();
		return type != null && type.isSurvival();
	}
}
