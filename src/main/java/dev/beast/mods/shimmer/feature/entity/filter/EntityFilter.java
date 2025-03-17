package dev.beast.mods.shimmer.feature.entity.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerEntity;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.function.Function;
import java.util.function.Predicate;

public interface EntityFilter extends Predicate<Entity> {
	SimpleRegistry<EntityFilter> REGISTRY = SimpleRegistry.create(EntityFilter::type);

	SimpleRegistryType.Unit<EntityFilter> NONE = SimpleRegistryType.unit(Shimmer.id("none"), entity -> false);
	SimpleRegistryType.Unit<EntityFilter> ANY = SimpleRegistryType.unit(Shimmer.id("any"), entity -> true);
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
	SimpleRegistryType.Unit<EntityFilter> VISIBLE = SimpleRegistryType.unit(Shimmer.id("visible"), entity -> !entity.isInvisible());
	SimpleRegistryType.Unit<EntityFilter> INVISIBLE = SimpleRegistryType.unit(Shimmer.id("invisible"), Entity::isInvisible);
	SimpleRegistryType.Unit<EntityFilter> SUSPENDED = SimpleRegistryType.unit(Shimmer.id("suspended"), ShimmerEntity::isSuspended);
	SimpleRegistryType.Unit<EntityFilter> GLOWING = SimpleRegistryType.unit(Shimmer.id("glowing"), Entity::isCurrentlyGlowing);

	static EntityFilter of(boolean value) {
		return value ? ANY.instance() : NONE.instance();
	}

	Codec<EntityFilter> CODEC = Codec.either(Codec.BOOL, REGISTRY.valueCodec()).xmap(either -> either.map(EntityFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	StreamCodec<RegistryFriendlyByteBuf, EntityFilter> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.BOOL, REGISTRY.valueStreamCodec()).map(either -> either.map(EntityFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	KnownCodec<EntityFilter> KNOWN_CODEC = KnownCodec.register(Shimmer.id("entity_filter"), CODEC, EntityFilter.class);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(EntityNotFilter.TYPE);
		REGISTRY.register(EntityAndFilter.TYPE);
		REGISTRY.register(EntityOrFilter.TYPE);
		REGISTRY.register(EntityXorFilter.TYPE);

		REGISTRY.register(NONE);
		REGISTRY.register(ANY);
		REGISTRY.register(ALIVE);
		REGISTRY.register(DEAD);
		REGISTRY.register(LIVING);
		REGISTRY.register(PLAYER);
		REGISTRY.register(SURVIVAL_PLAYER);
		REGISTRY.register(SURVIVAL_LIKE_PLAYER);
		REGISTRY.register(SPECTATOR);
		REGISTRY.register(CREATIVE);
		REGISTRY.register(SPECTATOR_OR_CREATIVE);
		REGISTRY.register(ITEM);
		REGISTRY.register(PROJECTILE);
		REGISTRY.register(VISIBLE);
		REGISTRY.register(INVISIBLE);
		REGISTRY.register(SUSPENDED);
		REGISTRY.register(GLOWING);

		REGISTRY.register(EntityTagFilter.TYPE);
		REGISTRY.register(EntityTypeFilter.TYPE);
		REGISTRY.register(EntityTypeTagFilter.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}
}
