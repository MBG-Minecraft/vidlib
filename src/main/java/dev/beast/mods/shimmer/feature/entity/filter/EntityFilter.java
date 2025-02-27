package dev.beast.mods.shimmer.feature.entity.filter;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.function.Predicate;

public enum EntityFilter implements StringRepresentable, Predicate<Entity> {
	NONE("none", entity -> false),
	ALL("all", entity -> true),
	ALIVE("alive", Entity::isAlive),
	DEAD("dead", entity -> !entity.isAlive()),
	LIVING("living", entity -> entity instanceof LivingEntity),
	PLAYER("player", entity -> entity instanceof Player),
	SURVIVAL_PLAYER("survival_player", entity -> entity instanceof Player player && !player.isCreative() && !player.isSpectator()),
	SPECTATOR("spectator", entity -> entity instanceof Player player && !player.isCreative() && !player.isSpectator()),
	CREATIVE("creative", entity -> entity instanceof Player player && !player.isCreative() && !player.isSpectator()),
	SPECTATOR_OR_CREATIVE("spectator_or_creative", entity -> entity instanceof Player player && !player.isCreative() && !player.isSpectator()),
	ITEMS("item", entity -> entity instanceof ItemEntity),
	PROJECTILES("projectile", entity -> entity instanceof Projectile),

	;

	public static final EntityFilter[] VALUES = values();
	public static final Codec<EntityFilter> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, EntityFilter> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

	private final String name;
	private final Predicate<? super Entity> predicate;

	EntityFilter(String name, Predicate<? super Entity> predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	@Override
	public boolean test(Entity entity) {
		return predicate.test(entity);
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
