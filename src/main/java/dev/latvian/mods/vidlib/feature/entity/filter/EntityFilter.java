package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolderList;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.util.StringUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EntityFilter extends Predicate<Entity> {
	SimpleRegistry<EntityFilter> REGISTRY = SimpleRegistry.create(EntityFilter::type);
	ImBuilderHolderList<EntityFilter> IMGUI_BUILDERS = new ImBuilderHolderList<>();

	SimpleRegistryType.Unit<EntityFilter> NONE = SimpleRegistryType.unit("none", new BasicEntityFilter(entity -> false));
	SimpleRegistryType.Unit<EntityFilter> ANY = SimpleRegistryType.unit("any", new BasicEntityFilter(entity -> true));

	SimpleRegistryType.Unit<EntityFilter> ALIVE = SimpleRegistryType.unit("alive", new BasicEntityFilter(Entity::isAlive));
	SimpleRegistryType.Unit<EntityFilter> DEAD = SimpleRegistryType.unit("dead", new BasicEntityFilter(entity -> !entity.isAlive()));
	SimpleRegistryType.Unit<EntityFilter> DEAD_OR_DYING = SimpleRegistryType.unit("dead_or_dying", new BasicEntityFilter(entity -> entity instanceof LivingEntity living ? living.isDeadOrDying() : !entity.isAlive()));
	SimpleRegistryType.Unit<EntityFilter> LIVING = SimpleRegistryType.unit("living", new BasicEntityFilter(entity -> entity instanceof LivingEntity));
	SimpleRegistryType.Unit<EntityFilter> PLAYER = SimpleRegistryType.unit("player", new BasicEntityFilter(entity -> entity instanceof Player));
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_PLAYER = SimpleRegistryType.unit("survival_player", new BasicEntityFilter(VLEntity::isSurvival));
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_LIKE_PLAYER = SimpleRegistryType.unit("survival_like_player", new BasicEntityFilter(VLEntity::isSurvivalLike));
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR = SimpleRegistryType.unit("spectator", new BasicEntityFilter(Entity::isSpectator));
	SimpleRegistryType.Unit<EntityFilter> CREATIVE = SimpleRegistryType.unit("creative", new BasicEntityFilter(entity -> entity instanceof Player player && player.isCreative()));
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR_OR_CREATIVE = SimpleRegistryType.unit("spectator_or_creative", new BasicEntityFilter(VLEntity::isSpectatorOrCreative));
	SimpleRegistryType.Unit<EntityFilter> ITEM = SimpleRegistryType.unit("item", new BasicEntityFilter(VLEntity::isItemEntity));
	SimpleRegistryType.Unit<EntityFilter> PROJECTILE = SimpleRegistryType.unit("projectile", new BasicEntityFilter(VLEntity::isProjectile));
	SimpleRegistryType.Unit<EntityFilter> VISIBLE = SimpleRegistryType.unit("visible", new BasicEntityFilter(VLEntity::isVisible));
	SimpleRegistryType.Unit<EntityFilter> INVISIBLE = SimpleRegistryType.unit("invisible", new BasicEntityFilter(Entity::isInvisible));
	SimpleRegistryType.Unit<EntityFilter> SUSPENDED = SimpleRegistryType.unit("suspended", new BasicEntityFilter(VLEntity::isSuspended));
	SimpleRegistryType.Unit<EntityFilter> GLOWING = SimpleRegistryType.unit("glowing", new BasicEntityFilter(Entity::isCurrentlyGlowing));
	SimpleRegistryType.Unit<EntityFilter> IN_WATER = SimpleRegistryType.unit("in_water", new BasicEntityFilter(Entity::isInWater));
	SimpleRegistryType.Unit<EntityFilter> IN_WATER_OR_RAIN = SimpleRegistryType.unit("in_water_or_rain", new BasicEntityFilter(Entity::isInWaterOrRain));
	SimpleRegistryType.Unit<EntityFilter> IN_LIQUID = SimpleRegistryType.unit("in_liquid", new BasicEntityFilter(Entity::isInLiquid));
	SimpleRegistryType.Unit<EntityFilter> UNDERWATER = SimpleRegistryType.unit("underwater", new BasicEntityFilter(Entity::isUnderWater));

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

	Codec<EntityFilter> CODEC = KLibCodecs.or(List.of(NONE_OR_ANY_CODEC, ExactEntityFilter.TYPE.codec().codec(), MatchEntityFilter.OPTIONAL_MATCH_CODEC, REGISTRY.valueCodec()));
	StreamCodec<RegistryFriendlyByteBuf, EntityFilter> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.BOOL, REGISTRY.valueStreamCodec()).map(either -> either.map(EntityFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	DataType<EntityFilter> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, EntityFilter.class);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(NONE);
		REGISTRY.register(ANY);

		REGISTRY.register(EntityNotFilter.TYPE);
		REGISTRY.register(EntityAndFilter.TYPE);
		REGISTRY.register(EntityOrFilter.TYPE);
		REGISTRY.register(EntityXorFilter.TYPE);

		REGISTRY.register(ALIVE);
		REGISTRY.register(DEAD);
		REGISTRY.register(DEAD_OR_DYING);
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
		REGISTRY.register(IN_WATER);
		REGISTRY.register(IN_WATER_OR_RAIN);
		REGISTRY.register(IN_LIQUID);
		REGISTRY.register(UNDERWATER);

		REGISTRY.register(ExactEntityFilter.TYPE);
		REGISTRY.register(EntityTagFilter.TYPE);
		REGISTRY.register(EntityTypeFilter.TYPE);
		REGISTRY.register(EntityTypeTagFilter.TYPE);
		REGISTRY.register(MatchEntityFilter.TYPE);
		REGISTRY.register(HasEffectEntityFilter.TYPE);

		for (var unit : REGISTRY.unitValueMap().entrySet()) {
			IMGUI_BUILDERS.addUnit(StringUtils.snakeCaseToTitleCase(unit.getKey()), unit.getValue());
		}

		IMGUI_BUILDERS.add(EntityNotFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(EntityAndFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(EntityOrFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(EntityXorFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(ExactEntityFilter.IDBuilder.TYPE);
		IMGUI_BUILDERS.add(ExactEntityFilter.UUIDBuilder.TYPE);
		IMGUI_BUILDERS.add(EntityTagFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(EntityTypeFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(EntityTypeTagFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(MatchEntityFilter.Builder.TYPE);
		IMGUI_BUILDERS.add(HasEffectEntityFilter.Builder.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default void writeUUID(FriendlyByteBuf buf) {
		buf.writeUtf(type().id());
		buf.writeUtf(toString());
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
}
