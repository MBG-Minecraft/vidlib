package dev.beast.mods.shimmer.feature.entity.filter;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerEntity;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.function.Predicate;

public interface EntityFilter extends Predicate<Entity> {
	SimpleRegistry<EntityFilter> REGISTRY = SimpleRegistry.create(EntityFilter::type);

	SimpleRegistryType.Unit<EntityFilter> NONE = SimpleRegistryType.unit(Shimmer.id("none"), entity -> false);
	SimpleRegistryType.Unit<EntityFilter> ALL = SimpleRegistryType.unit(Shimmer.id("all"), entity -> true);
	SimpleRegistryType.Unit<EntityFilter> ALIVE = SimpleRegistryType.unit(Shimmer.id("alive"), Entity::isAlive);
	SimpleRegistryType.Unit<EntityFilter> DEAD = SimpleRegistryType.unit(Shimmer.id("dead"), entity -> !entity.isAlive());
	SimpleRegistryType.Unit<EntityFilter> LIVING = SimpleRegistryType.unit(Shimmer.id("living"), entity -> entity instanceof LivingEntity);
	SimpleRegistryType.Unit<EntityFilter> PLAYER = SimpleRegistryType.unit(Shimmer.id("player"), entity -> entity instanceof Player);
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_PLAYER = SimpleRegistryType.unit(Shimmer.id("survival_player"), ShimmerEntity::isSurvival);
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_LIKE_PLAYER = SimpleRegistryType.unit(Shimmer.id("survival_like_player"), ShimmerEntity::isSurvivalLike);
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR = SimpleRegistryType.unit(Shimmer.id("spectator"), Entity::isSpectator);
	SimpleRegistryType.Unit<EntityFilter> CREATIVE = SimpleRegistryType.unit(Shimmer.id("creative"), entity -> entity instanceof Player player && player.isCreative());
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR_OR_CREATIVE = SimpleRegistryType.unit(Shimmer.id("spectator_or_creative"), ShimmerEntity::isSpectatorOrCreative);
	SimpleRegistryType.Unit<EntityFilter> ITEM = SimpleRegistryType.unit(Shimmer.id("item"), entity -> entity instanceof ItemEntity);
	SimpleRegistryType.Unit<EntityFilter> PROJECTILE = SimpleRegistryType.unit(Shimmer.id("projectile"), entity -> entity instanceof Projectile);

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}
}
