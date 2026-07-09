package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.UnitType;
import dev.latvian.mods.klib.util.IntOrUUID;
import dev.latvian.mods.klib.util.ParsedEntitySelector;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EntityFilter extends Predicate<Entity>, CustomRegistryValue<RegistryFriendlyByteBuf, EntityFilter> {
	static UnitType<RegistryFriendlyByteBuf, EntityFilter> basic(String name, Predicate<Entity> predicate) {
		return UnitType.create(name, type -> new BasicEntityFilter(type, predicate));
	}

	UnitType<RegistryFriendlyByteBuf, EntityFilter> NONE = basic("none", _ -> false);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> ANY = basic("any", _ -> true);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> ALIVE = basic("alive", Entity::isAlive);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> DEAD = basic("dead", entity -> !entity.isAlive());
	UnitType<RegistryFriendlyByteBuf, EntityFilter> DEAD_OR_DYING = basic("dead_or_dying", entity -> !entity.isAlive());
	UnitType<RegistryFriendlyByteBuf, EntityFilter> LIVING = basic("living", entity -> entity instanceof LivingEntity);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> MOB = basic("mob", entity -> entity instanceof Mob);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> ENEMY = basic("enemy", entity -> entity instanceof Enemy);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> PLAYER = basic("player", entity -> entity instanceof Player);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> SURVIVAL_MODE = basic("survival_mode", entity -> entity.getGameMode() == GameType.SURVIVAL);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> ADVENTURE_MODE = basic("adventure_mode", entity -> entity.getGameMode() == GameType.ADVENTURE);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> SURVIVAL_LIKE_MODE = basic("survival_like_mode", entity -> entity.getGameMode() != null && entity.getGameMode().isSurvival());
	UnitType<RegistryFriendlyByteBuf, EntityFilter> CREATIVE_MODE = basic("creative_mode", entity -> entity.getGameMode() == GameType.CREATIVE);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> SPECTATOR_MODE = basic("spectator_mode", Entity::isSpectator);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> SPECTATOR_OR_CREATIVE_MODE = basic("spectator_or_creative_mode", entity -> entity.getGameMode() == GameType.SPECTATOR || entity.getGameMode() == GameType.CREATIVE);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> ITEM = basic("item", entity -> entity instanceof ItemEntity);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> PROJECTILE = basic("projectile", entity -> entity instanceof Projectile);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> VISIBLE = basic("visible", entity -> !entity.isInvisible());
	UnitType<RegistryFriendlyByteBuf, EntityFilter> INVISIBLE = basic("invisible", Entity::isInvisible);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> SUSPENDED = basic("suspended", entity -> CommonGameEngine.INSTANCE.isSuspended(entity));
	UnitType<RegistryFriendlyByteBuf, EntityFilter> GLOWING = basic("glowing", Entity::isCurrentlyGlowing);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> IN_WATER = basic("in_water", Entity::isInWater);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> IN_WATER_OR_RAIN = basic("in_water_or_rain", Entity::isInWaterOrRain);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> IN_LIQUID = basic("in_liquid", Entity::isInLiquid);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> UNDERWATER = basic("underwater", Entity::isUnderWater);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> ON_RAILS = basic("on_rails", Entity::isOnRails);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> ON_FIRE = basic("on_fire", Entity::isOnFire);
	UnitType<RegistryFriendlyByteBuf, EntityFilter> STAFF = basic("staff", entity -> VLPlatformHelper.CURRENT.isStaff(entity));
	UnitType<RegistryFriendlyByteBuf, EntityFilter> STAFF_OR_TALENT = basic("staff_or_talent", entity -> VLPlatformHelper.CURRENT.isStaffOrTalent(entity));

	static EntityFilter of(boolean value) {
		return value ? ANY.value() : NONE.value();
	}

	CustomRegistry<RegistryFriendlyByteBuf, EntityFilter> REGISTRY = CustomRegistry.create("entity_filter");

	Codec<EntityFilter> NONE_OR_ANY_CODEC = Codec.BOOL.flatXmap(b -> DataResult.success(of(b)), filter -> {
		if (filter == ANY.value()) {
			return DataResult.success(true);
		} else if (filter == NONE.value()) {
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
	DataType<EntityFilter> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, EntityFilter> registry) {
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
		registry.register(STAFF);
		registry.register(STAFF_OR_TALENT);

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
	default CustomRegistry<RegistryFriendlyByteBuf, EntityFilter> getRegistry() {
		return REGISTRY;
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
		if (filter == ANY.value()) {
			return this;
		} else if (filter == NONE.value()) {
			return filter;
		} else {
			return new EntityAndFilter(List.of(this, filter));
		}
	}

	default EntityFilter and(EntityFilter... filters) {
		var list = new ArrayList<EntityFilter>(filters.length + 1);

		if (this != ANY.value()) {
			list.add(this);
		}

		for (var filter : filters) {
			if (filter == NONE.value()) {
				return filter;
			} else if (filter != ANY.value()) {
				list.add(filter);
			}
		}

		return new EntityAndFilter(List.copyOf(list));
	}

	default EntityFilter or(EntityFilter filter) {
		if (filter == ANY.value()) {
			return filter;
		} else if (filter == NONE.value()) {
			return this;
		} else {
			return new EntityOrFilter(List.of(this, filter));
		}
	}

	default EntityFilter or(EntityFilter... filters) {
		var list = new ArrayList<EntityFilter>(filters.length + 1);

		if (this != NONE.value()) {
			list.add(this);
		}

		for (var filter : filters) {
			if (filter == ANY.value()) {
				return filter;
			} else if (filter != NONE.value()) {
				list.add(filter);
			}
		}

		return new EntityOrFilter(List.copyOf(list));
	}
}
