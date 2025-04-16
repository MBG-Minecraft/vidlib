package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.FriendlyByteBuf;
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

	SimpleRegistryType.Unit<EntityFilter> NONE = SimpleRegistryType.unit(VidLib.id("none"), new BasicEntityFilter(entity -> false));
	SimpleRegistryType.Unit<EntityFilter> ANY = SimpleRegistryType.unit(VidLib.id("any"), new BasicEntityFilter(entity -> true));

	SimpleRegistryType.Unit<EntityFilter> ALIVE = SimpleRegistryType.unit(VidLib.id("alive"), new BasicEntityFilter(Entity::isAlive));
	SimpleRegistryType.Unit<EntityFilter> DEAD = SimpleRegistryType.unit(VidLib.id("dead"), new BasicEntityFilter(entity -> !entity.isAlive()));
	SimpleRegistryType.Unit<EntityFilter> LIVING = SimpleRegistryType.unit(VidLib.id("living"), new BasicEntityFilter(entity -> entity instanceof LivingEntity));
	SimpleRegistryType.Unit<EntityFilter> PLAYER = SimpleRegistryType.unit(VidLib.id("player"), new BasicEntityFilter(entity -> entity instanceof Player));
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_PLAYER = SimpleRegistryType.unit(VidLib.id("survival_player"), new BasicEntityFilter(VLEntity::isSurvival));
	SimpleRegistryType.Unit<EntityFilter> SURVIVAL_LIKE_PLAYER = SimpleRegistryType.unit(VidLib.id("survival_like_player"), new BasicEntityFilter(VLEntity::isSurvivalLike));
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR = SimpleRegistryType.unit(VidLib.id("spectator"), new BasicEntityFilter(Entity::isSpectator));
	SimpleRegistryType.Unit<EntityFilter> CREATIVE = SimpleRegistryType.unit(VidLib.id("creative"), new BasicEntityFilter(entity -> entity instanceof Player player && player.isCreative()));
	SimpleRegistryType.Unit<EntityFilter> SPECTATOR_OR_CREATIVE = SimpleRegistryType.unit(VidLib.id("spectator_or_creative"), new BasicEntityFilter(VLEntity::isSpectatorOrCreative));
	SimpleRegistryType.Unit<EntityFilter> ITEM = SimpleRegistryType.unit(VidLib.id("item"), new BasicEntityFilter(entity -> entity instanceof ItemEntity));
	SimpleRegistryType.Unit<EntityFilter> PROJECTILE = SimpleRegistryType.unit(VidLib.id("projectile"), new BasicEntityFilter(entity -> entity instanceof Projectile));
	SimpleRegistryType.Unit<EntityFilter> VISIBLE = SimpleRegistryType.unit(VidLib.id("visible"), new BasicEntityFilter(entity -> !entity.isInvisible()));
	SimpleRegistryType.Unit<EntityFilter> INVISIBLE = SimpleRegistryType.unit(VidLib.id("invisible"), new BasicEntityFilter(Entity::isInvisible));
	SimpleRegistryType.Unit<EntityFilter> SUSPENDED = SimpleRegistryType.unit(VidLib.id("suspended"), new BasicEntityFilter(VLEntity::isSuspended));
	SimpleRegistryType.Unit<EntityFilter> GLOWING = SimpleRegistryType.unit(VidLib.id("glowing"), new BasicEntityFilter(Entity::isCurrentlyGlowing));

	static EntityFilter of(boolean value) {
		return value ? ANY.instance() : NONE.instance();
	}

	Codec<EntityFilter> CODEC = Codec.either(Codec.BOOL, REGISTRY.valueCodec()).xmap(either -> either.map(EntityFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	StreamCodec<RegistryFriendlyByteBuf, EntityFilter> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.BOOL, REGISTRY.valueStreamCodec()).map(either -> either.map(EntityFilter::of, Function.identity()), filter -> filter == ANY.instance() ? Either.left(true) : filter == NONE.instance() ? Either.left(false) : Either.right(filter));
	KnownCodec<EntityFilter> KNOWN_CODEC = KnownCodec.register(VidLib.id("entity_filter"), CODEC, STREAM_CODEC, EntityFilter.class);

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

	default void writeUUID(FriendlyByteBuf buf) {
		buf.writeUtf(type().id().toString());
		buf.writeUtf(toString());
	}
}
