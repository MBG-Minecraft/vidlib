package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.IntOrUUID;
import dev.latvian.mods.klib.util.ParsedEntitySelector;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EntityFilter extends Predicate<Entity>, SimpleRegistryEntry {
	SimpleRegistry<EntityFilter> REGISTRY = SimpleRegistry.create(VidLib.id("entity_filter"), c -> PlatformHelper.CURRENT.collectEntityFilters(c));

	static SimpleRegistryType.Unit<EntityFilter> basic(String name, Predicate<Entity> predicate) {
		return SimpleRegistryType.unitWithType(name, type -> new BasicEntityFilter(type, predicate));
	}

	SimpleRegistryType.Unit<EntityFilter> NONE = basic("none", entity -> false);
	SimpleRegistryType.Unit<EntityFilter> ANY = basic("any", entity -> true);
	SimpleRegistryType.Unit<EntityFilter> ALIVE = basic("alive", Entity::isAlive);
	SimpleRegistryType.Unit<EntityFilter> DEAD = basic("dead", entity -> !entity.isAlive());
	SimpleRegistryType.Unit<EntityFilter> DEAD_OR_DYING = basic("dead_or_dying", VLEntity::vl$isDeadOrDying);
	SimpleRegistryType.Unit<EntityFilter> LIVING = basic("living", entity -> entity instanceof LivingEntity);
	SimpleRegistryType.Unit<EntityFilter> MOB = basic("mob", entity -> entity instanceof Mob);
	SimpleRegistryType.Unit<EntityFilter> ENEMY = basic("enemy", entity -> entity instanceof Enemy);
	SimpleRegistryType.Unit<EntityFilter> PLAYER = basic("player", entity -> entity instanceof Player);
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_MODE = basic("survival_mode", VLEntity::isSurvival);
	SimpleRegistryType.Unit<EntityFilter> ADVENTURE_MODE = basic("adventure_mode", VLEntity::isAdventure);
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_LIKE_MODE = basic("survival_like_mode", VLEntity::isSurvivalLike);
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR_MODE = basic("spectator_mode", Entity::isSpectator);
	SimpleRegistryType.Unit<EntityFilter> CREATIVE_MODE = basic("creative_mode", VLEntity::vl$isCreative);
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR_OR_CREATIVE_MODE = basic("spectator_or_creative_mode", VLEntity::isSpectatorOrCreative);
	SimpleRegistryType.Unit<EntityFilter> ITEM = basic("item", VLEntity::isItemEntity);
	SimpleRegistryType.Unit<EntityFilter> PROJECTILE = basic("projectile", VLEntity::isProjectile);
	SimpleRegistryType.Unit<EntityFilter> VISIBLE = basic("visible", VLEntity::isVisible);
	SimpleRegistryType.Unit<EntityFilter> INVISIBLE = basic("invisible", Entity::isInvisible);
	SimpleRegistryType.Unit<EntityFilter> SUSPENDED = basic("suspended", VLEntity::vl$isSuspended);
	SimpleRegistryType.Unit<EntityFilter> GLOWING = basic("glowing", Entity::isCurrentlyGlowing);
	SimpleRegistryType.Unit<EntityFilter> IN_WATER = basic("in_water", Entity::isInWater);
	SimpleRegistryType.Unit<EntityFilter> IN_WATER_OR_RAIN = basic("in_water_or_rain", Entity::isInWaterOrRain);
	SimpleRegistryType.Unit<EntityFilter> IN_LIQUID = basic("in_liquid", Entity::isInLiquid);
	SimpleRegistryType.Unit<EntityFilter> UNDERWATER = basic("underwater", Entity::isUnderWater);
	SimpleRegistryType.Unit<EntityFilter> ON_RAILS = basic("on_rails", Entity::isOnRails);
	SimpleRegistryType.Unit<EntityFilter> ON_FIRE = basic("on_fire", Entity::isOnFire);

	static EntityFilter of(boolean value) {
		return value ? ANY.instance() : NONE.instance();
	}

	Codec<EntityFilter> NONE_OR_ANY_CODEC = Codec.BOOL.flatXmap(b -> DataResult.success(of(b)), filter -> {
		if (filter == ANY.instance()) {
			return DataResult.success(true);
		} else if (filter == NONE.instance()) {
			return DataResult.success(false);
		} else {
			return DataResult.error(() -> "Expected either 'any' or 'none'");
		}
	});

	Codec<EntityFilter> FROM_INT_OR_UUID_CODEC = IntOrUUID.CODEC.flatXmap(input -> DataResult.success(new ExactEntityFilter(input)), filter -> {
		if (filter instanceof ExactEntityFilter(IntOrUUID id)) {
			return DataResult.success(id);
		} else {
			return DataResult.error(() -> "Filter is not an ExactEntityFilter");
		}
	});

	Codec<EntityFilter> FROM_STRING_CODEC = Codec.STRING.flatXmap(input -> {
		if (input.startsWith("@")) {
			return ParsedEntitySelector.CODEC.parse(JavaOps.INSTANCE, input).map(MatchEntityFilter::new);
		} else if (input.startsWith("$$")) {
			return DataResult.success(new PlayerDataEntityFilter(input.substring(2)));
		} else if (input.startsWith("$")) {
			return DataResult.success(new ServerDataEntityFilter(input.substring(1)));
		} else {
			return DataResult.error(() -> "String does not start with '@', '$$' or '$'");
		}
	}, filter -> switch (filter) {
		case MatchEntityFilter(ParsedEntitySelector s) -> DataResult.success(s.getInput());
		case PlayerDataEntityFilter(DataKey<?> dataKey) -> DataResult.success("$$" + dataKey.id());
		case ServerDataEntityFilter(DataKey<?> dataKey) -> DataResult.success("$" + dataKey.id());
		case null, default -> DataResult.error(() -> "Filter is not a MatchEntityFilter, PlayerDataEntityFilter or ServerDataEntityFilter");
	});

	Codec<EntityFilter> CODEC = KLibCodecs.or(List.of(
		NONE_OR_ANY_CODEC,
		FROM_INT_OR_UUID_CODEC,
		FROM_STRING_CODEC,
		REGISTRY.codec()
	));

	StreamCodec<RegistryFriendlyByteBuf, EntityFilter> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.BOOL, REGISTRY.streamCodec()).map(either -> either.map(EntityFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	DataType<EntityFilter> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, EntityFilter.class);

	static void builtinTypes(SimpleRegistryCollector<EntityFilter> registry) {
		registry.register(NONE);
		registry.register(ANY);

		registry.register(EntityNotFilter.TYPE);
		registry.register(EntityAndFilter.TYPE);
		registry.register(EntityOrFilter.TYPE);
		registry.register(EntityXorFilter.TYPE);

		registry.register(ALIVE);
		registry.register(DEAD);
		registry.register(DEAD_OR_DYING);
		registry.register(LIVING);
		registry.register(MOB);
		registry.register(ENEMY);
		registry.register(PLAYER);
		registry.register(SURVIVAL_MODE);
		registry.register(ADVENTURE_MODE);
		registry.register(SURVIVAL_LIKE_MODE);
		registry.register(SPECTATOR_MODE);
		registry.register(CREATIVE_MODE);
		registry.register(SPECTATOR_OR_CREATIVE_MODE);
		registry.register(ITEM);
		registry.register(PROJECTILE);
		registry.register(VISIBLE);
		registry.register(INVISIBLE);
		registry.register(SUSPENDED);
		registry.register(GLOWING);
		registry.register(IN_WATER);
		registry.register(IN_WATER_OR_RAIN);
		registry.register(IN_LIQUID);
		registry.register(UNDERWATER);
		registry.register(ON_RAILS);
		registry.register(ON_FIRE);

		registry.register(ExactEntityFilter.TYPE);
		registry.register(EntityTagFilter.TYPE);
		registry.register(EntityTypeFilter.TYPE);
		registry.register(EntityTypeTagFilter.TYPE);
		registry.register(MatchEntityFilter.TYPE);
		registry.register(HasEffectEntityFilter.TYPE);
		registry.register(ServerDataEntityFilter.TYPE);
		registry.register(PlayerDataEntityFilter.TYPE);
		registry.register(ProfileEntityFilter.TYPE);
		registry.register(HasItemEntityFilter.TYPE);
		registry.register(InDimensionEntityFilter.TYPE);
		registry.register(IfEntityFilter.TYPE);
	}

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	@Nullable
	default Entity getFirst(Level level) {
		for (var entity : level.allEntities()) {
			if (test(entity)) {
				return entity;
			}
		}

		return null;
	}

	default EntityFilter not() {
		return new EntityNotFilter(this);
	}

	default EntityFilter and(EntityFilter filter) {
		if (filter == ANY.instance()) {
			return this;
		} else if (filter == NONE.instance()) {
			return filter;
		} else {
			return new EntityAndFilter(List.of(this, filter));
		}
	}

	default EntityFilter and(EntityFilter... filters) {
		var list = new ArrayList<EntityFilter>(filters.length + 1);

		if (this != ANY.instance()) {
			list.add(this);
		}

		for (var filter : filters) {
			if (filter == NONE.instance()) {
				return filter;
			} else if (filter != ANY.instance()) {
				list.add(filter);
			}
		}

		return new EntityAndFilter(List.copyOf(list));
	}

	default EntityFilter or(EntityFilter filter) {
		if (filter == ANY.instance()) {
			return filter;
		} else if (filter == NONE.instance()) {
			return this;
		} else {
			return new EntityOrFilter(List.of(this, filter));
		}
	}

	default EntityFilter or(EntityFilter... filters) {
		var list = new ArrayList<EntityFilter>(filters.length + 1);

		if (this != NONE.instance()) {
			list.add(this);
		}

		for (var filter : filters) {
			if (filter == ANY.instance()) {
				return filter;
			} else if (filter != NONE.instance()) {
				list.add(filter);
			}
		}

		return new EntityOrFilter(List.copyOf(list));
	}
}
