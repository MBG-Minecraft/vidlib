package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.vidlib.feature.entity.C2SEntityEventPayload;
import dev.latvian.mods.vidlib.feature.entity.EntityData;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.entity.EntityOverrideValue;
import dev.latvian.mods.vidlib.feature.entity.ForceEntityVelocityPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.S2CEntityEventPayload;
import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBar;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import dev.latvian.mods.vidlib.math.worldposition.EntityPositionType;
import net.minecraft.core.BlockPos;
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

public interface VLEntity extends VLLevelContainer, PlayerActionHandler {
	default Entity vl$self() {
		return (Entity) this;
	}

	@Override
	default Level vl$level() {
		return vl$self().level();
	}

	@Nullable
	default Map<EntityOverride<?>, EntityOverrideValue<?>> vl$getEntityOverridesMap() {
		throw new NoMixinException(this);
	}

	default void vl$setEntityOverridesMap(@Nullable Map<EntityOverride<?>, EntityOverrideValue<?>> map) {
		throw new NoMixinException(this);
	}

	@SuppressWarnings("unchecked")
	default <T> T vl$getDirectOverride(EntityOverride<T> override) {
		var map = vl$getEntityOverridesMap();
		var v = map == null ? null : (EntityOverrideValue<T>) map.get(override);
		return v == null ? null : v.get(vl$self());
	}

	default <T> void vl$setDirectOverride(EntityOverride<T> override, @Nullable EntityOverrideValue<T> value) {
		var map = vl$getEntityOverridesMap();

		if (value == null) {
			if (map != null) {
				map.remove(override);

				if (map.isEmpty()) {
					vl$setEntityOverridesMap(null);
				}
			}
		} else {
			if (map == null) {
				map = new IdentityHashMap<>(1);
				vl$setEntityOverridesMap(map);
			}

			map.put(override, value);
		}
	}

	default boolean vl$isSaving() {
		return false;
	}

	default List<ZoneInstance> getZones() {
		var zones = vl$level().vl$getActiveZones();
		return zones == null ? List.of() : zones.entityZones.getOrDefault((vl$self()).getId(), List.of());
	}

	@Nullable
	default GameType getGameMode() {
		return null;
	}

	default boolean isSpectatorOrCreative() {
		var type = getGameMode();
		return type == GameType.SPECTATOR || type == GameType.CREATIVE;
	}

	default boolean vl$isCreative() {
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
	default Boolean vl$glowingOverride() {
		return EntityOverride.GLOWING.get(this);
	}

	@Nullable
	default Integer vl$teamColorOverride() {
		var col = EntityOverride.TEAM_COLOR.get(this);
		return col == null ? null : col.rgb();
	}

	default double vl$gravityMod() {
		return EntityOverride.GRAVITY.get(this, 1D);
	}

	default float vl$speedMod() {
		return EntityOverride.SPEED.get(this, 1F);
	}

	default float vl$attackDamageMod() {
		return EntityOverride.ATTACK_DAMAGE.get(this, 1F);
	}

	default Line ray(double distance, float delta) {
		var start = vl$self().getEyePosition(delta);
		var end = start.add(vl$self().getViewVector(delta).scale(distance));
		return new Line(start, end);
	}

	default Line ray(float delta) {
		return ray(4.5D, delta);
	}

	default void teleport(ServerLevel to, Vec3 pos) {
		var entity = vl$self();
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
		teleport((ServerLevel) vl$level(), pos);
	}

	default void teleport(ServerLevel to, BlockPos pos) {
		teleport(to, new Vec3(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D));
	}

	default void teleport(BlockPos pos) {
		teleport(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D));
	}

	default void teleport(Location location) {
		var entity = vl$self();
		teleport(entity.getServer().getLevel(location.dimension()), location.random(entity.getRandom(), new Vec3(0.5D, 0.1D, 0.5D)));
	}

	default void forceSetVelocity(Vec3 velocity) {
		var e = vl$self();
		e.setDeltaMovement(velocity);

		if (!e.level().isClientSide()) {
			e.level().s2c(new ForceEntityVelocityPayload(e.getId(), velocity));
		}
	}

	default void forceAddVelocity(Vec3 velocity) {
		forceSetVelocity(vl$self().getDeltaMovement().add(velocity));
	}

	default void playSound(SoundData data, boolean looping, boolean stopImmediately) {
		var e = vl$self();
		e.level().playGlobalSound(new PositionedSoundData(data, e, looping, stopImmediately), WorldNumberVariables.EMPTY);
	}

	default void playSound(SoundData data) {
		playSound(data, false, true);
	}

	default void s2c(EntityData data) {
		vl$level().s2c(new S2CEntityEventPayload(data));
	}

	default void s2cReceived(EntityData event, Player clientPlayer) {
	}

	default void c2s(EntityData data) {
		vl$level().c2s(new C2SEntityEventPayload(data));
	}

	default void c2sReceived(EntityData event, ServerPlayer from) {
	}

	default Vec3 getSoundSource(float delta) {
		return vl$self().getEyePosition(delta);
	}

	default Vec3 getLookTarget(float delta) {
		var e = vl$self();

		if (delta == 1F) {
			return e.position().add(e.getViewVector(1F));
		} else {
			return e.getPosition(delta).add(e.getViewVector(delta));
		}
	}

	default Vec3 getPosition(EntityPositionType type) {
		var e = vl$self();

		return switch (type) {
			case CENTER -> new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2D, e.getZ());
			case TOP -> new Vec3(e.getX(), e.getY() + e.getBbHeight(), e.getZ());
			case EYES -> e.getEyePosition();
			case LEASH -> e.position().add(e.getLeashOffset(1F));
			case SOUND_SOURCE -> getSoundSource(1F);
			case LOOK_TARGET -> getLookTarget(1F);
			default -> e.position();
		};
	}

	default Vec3 getPosition(EntityPositionType type, float delta) {
		var e = vl$self();

		return switch (type) {
			case CENTER -> e.getPosition(delta).add(0D, e.getBbHeight() / 2D, 0D);
			case TOP -> e.getPosition(delta).add(0D, e.getBbHeight(), 0D);
			case EYES -> e.getEyePosition(delta);
			case LEASH -> e.getPosition(delta).add(e.getLeashOffset(delta));
			case SOUND_SOURCE -> getSoundSource(delta);
			case LOOK_TARGET -> getLookTarget(delta);
			default -> delta == 1F ? e.position() : e.getPosition(delta);
		};
	}

	default float getRelativeHealth(float delta) {
		return 1F;
	}

	default boolean preventDismount(Player passenger) {
		return false;
	}

	default float getVehicleCameraDistance(Player passenger, float original) {
		return original;
	}

	default float getPassengerScale(Player passenger) {
		return 1F;
	}

	default PlayerInput getPilotInput() {
		return PlayerInput.NONE;
	}

	default void vl$setPilotInput(PlayerInput input) {
		throw new NoMixinException(this);
	}

	@Nullable
	default Boolean forceRenderVehicleCrosshair(Player passenger) {
		return null;
	}

	default Rotation rotation(float delta) {
		var e = vl$self();
		return Rotation.deg(e.getYRot(delta), e.getXRot(delta));
	}

	default Rotation viewRotation(float delta) {
		var e = vl$self();
		return Rotation.deg(e.getViewYRot(delta), e.getViewXRot(delta));
	}

	default boolean isMainBoss() {
		return vl$self().getTags().contains("main_boss");
	}

	@Nullable
	default ProgressBar getBossBar() {
		return isMainBoss() ? ProgressBar.DEFAULT_ENTITY : null;
	}
}
