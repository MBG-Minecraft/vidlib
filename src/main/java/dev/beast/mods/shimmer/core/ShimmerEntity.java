package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.entity.C2SEntityEventPayload;
import dev.beast.mods.shimmer.feature.entity.EntityData;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import dev.beast.mods.shimmer.feature.entity.ForceEntityVelocityPayload;
import dev.beast.mods.shimmer.feature.entity.S2CEntityEventPayload;
import dev.beast.mods.shimmer.feature.location.Location;
import dev.beast.mods.shimmer.feature.sound.SoundData;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.math.Line;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public interface ShimmerEntity extends ShimmerEntityContainer {
	@Override
	default Level shimmer$level() {
		return ((Entity) this).level();
	}

	@Nullable
	default Map<EntityOverride<?>, EntityOverrideValue<?>> shimmer$getEntityOverridesMap() {
		throw new NoMixinException(this);
	}

	default void shimmer$setEntityOverridesMap(@Nullable Map<EntityOverride<?>, EntityOverrideValue<?>> map) {
		throw new NoMixinException(this);
	}

	@SuppressWarnings("unchecked")
	default <T> T shimmer$getDirectOverride(EntityOverride<T> override) {
		var map = shimmer$getEntityOverridesMap();
		var v = map == null ? null : (EntityOverrideValue<T>) map.get(override);
		return v == null ? null : v.get((Entity) this);
	}

	default <T> void shimmer$setDirectOverride(EntityOverride<T> override, @Nullable EntityOverrideValue<T> value) {
		var map = shimmer$getEntityOverridesMap();

		if (value == null) {
			if (map != null) {
				map.remove(override);

				if (map.isEmpty()) {
					shimmer$setEntityOverridesMap(null);
				}
			}
		} else {
			if (map == null) {
				map = new IdentityHashMap<>(1);
				shimmer$setEntityOverridesMap(map);
			}

			map.put(override, value);
		}
	}

	default boolean shimmer$isSaving() {
		return false;
	}

	default List<ZoneInstance> getZones() {
		var zones = shimmer$level().shimmer$getActiveZones();
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

	default boolean shimmer$isCreative() {
		return getGameMode() == GameType.CREATIVE;
	}

	default boolean isSurvival() {
		return getGameMode() == GameType.SURVIVAL;
	}

	default boolean isSurvivalLike() {
		var type = getGameMode();
		return type != null && type.isSurvival();
	}

	default boolean isSuspended() {
		return EntityOverride.SUSPENDED.get(this, false);
	}

	@Nullable
	default Boolean shimmer$glowingOverride() {
		return EntityOverride.GLOWING.get(this);
	}

	@Nullable
	default Integer shimmer$teamColorOverride() {
		var col = EntityOverride.TEAM_COLOR.get(this);
		return col == null ? null : col.rgb();
	}

	default double shimmer$gravityMod() {
		return EntityOverride.GRAVITY.get(this, 1D);
	}

	default float shimmer$speedMod() {
		return EntityOverride.SPEED.get(this, 1F);
	}

	default float shimmer$attackDamageMod() {
		return EntityOverride.ATTACK_DAMAGE.get(this, 1F);
	}

	default Line ray(double distance, float delta) {
		var start = ((Entity) this).getEyePosition(delta);
		var end = start.add(((Entity) this).getViewVector(delta).scale(distance));
		return new Line(start, end);
	}

	default Line ray(float delta) {
		return ray(4.5D, delta);
	}

	default void teleport(ServerLevel to, Vec3 pos) {
		var entity = (Entity) this;
		entity.teleport(new TeleportTransition(
			to,
			pos,
			entity.getDeltaMovement(),
			entity.getYRot(),
			entity.getXRot(),
			TeleportTransition.DO_NOTHING
		));
	}

	default void teleport(Vec3 pos) {
		teleport((ServerLevel) ((Entity) this).level(), pos);
	}

	default void teleport(Location location) {
		teleport(((Entity) this).getServer().getLevel(location.dimension()), new Vec3(location.position().getX() + 0.5D, location.position().getY() + 0.1D, location.position().getZ() + 0.5D));
	}

	default void forceSetVelocity(Vec3 velocity) {
		var e = (Entity) this;
		e.setDeltaMovement(velocity);

		if (!e.level().isClientSide()) {
			e.level().s2c(new ForceEntityVelocityPayload(e.getId(), velocity));
		}
	}

	default void forceAddVelocity(Vec3 velocity) {
		forceSetVelocity(((Entity) this).getDeltaMovement().add(velocity));
	}

	default void playTrackingSound(SoundData data, boolean looping) {
		var e = (Entity) this;
		e.level().playTrackingSound(e, data, looping);
	}

	default void s2c(EntityData data) {
		shimmer$level().s2c(new S2CEntityEventPayload(data));
	}

	default void s2cReceived(EntityData event, Player player) {
	}

	default void c2s(EntityData data) {
		shimmer$level().c2s(new C2SEntityEventPayload(data));
	}

	default void c2sReceived(EntityData event, ServerPlayer from) {
	}
}
