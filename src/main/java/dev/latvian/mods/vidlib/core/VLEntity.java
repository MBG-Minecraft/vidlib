package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.entity.EntityUtils;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.feature.entity.C2SEntityEventPayload;
import dev.latvian.mods.vidlib.feature.entity.EntityData;
import dev.latvian.mods.vidlib.feature.entity.ForceEntityVelocityPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.S2CEntityEventPayload;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface VLEntity extends VLLevelContainer, PlayerActionHandler {
	default Entity vl$self() {
		return (Entity) this;
	}

	@Override
	default Level vl$level() {
		return vl$self().level();
	}

	default List<Zone> getZones() {
		var zones = vl$level().vl$getActiveZones();
		return zones == null ? List.of() : zones.entityZones.getOrDefault((vl$self()).getId(), List.of());
	}

	default boolean vl$isSuspended() {
		return CommonGameEngine.INSTANCE.isSuspended(vl$self());
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
		var ctx = entity.level().getGlobalContext();

		if (entity.level() instanceof ServerLevel serverLevel) {
			var to = serverLevel.getServer().getLevel(location.dimension());

			if (to != null) {
				teleport(to, location.sample(entity.getRandom()).value().get(ctx));
			}
		}
	}

	default void forceSetVelocity(Vec3 velocity) {
		var e = vl$self();
		e.setDeltaMovement(velocity);

		if (!e.level().isClientSide()) {
			e.level().s2c(new ForceEntityVelocityPayload(e.getId(), velocity));
		}
	}

	default void forceAddVelocity(Vec3 velocity) {
		forceAddVelocity(velocity.x, velocity.y, velocity.z);
	}

	default void forceAddVelocity(double dx, double dy, double dz) {
		forceSetVelocity(vl$self().getDeltaMovement().add(dx, dy, dz));
	}

	default void playSound(SoundData data, boolean looping, boolean stopImmediately) {
		var e = vl$self();
		e.level().playGlobalSound(new PositionedSoundData(data, e, looping, stopImmediately), KNumberVariables.EMPTY);
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

	default boolean preventDismount(Player passenger) {
		return false;
	}

	@Nullable
	default Component getCustomMountMessage() {
		return null;
	}

	default float getVehicleCameraDistance(Player passenger, float original) {
		return original;
	}

	default float getPassengerScale(Entity passenger) {
		return 1F;
	}

	default void setPilotInput(Player player, PlayerInput input) {
	}

	default void sortPassengers(List<Entity> passengers) {
	}

	default void replaySnapshot(VLS2CPacketConsumer packets) {
	}

	default void imgui(ImGraphics graphics, float delta) {
	}

	default boolean hideCrosshair(Player player) {
		return false;
	}

	default float getPassengerCameraRoll(Player player, double delta, float roll) {
		return roll;
	}

	default boolean shouldRenderPassengerHand(Player player) {
		return true;
	}

	// WIP
	default boolean overridePassengerClientLeftClick(Player player) {
		return false;
	}

	// WIP
	default boolean overridePassengerClientRightClick(Player player) {
		return false;
	}

	default boolean addTags(Collection<String> tags) {
		return vl$self().tags.addAll(tags);
	}

	default boolean removeTags(Collection<String> tags) {
		return vl$self().tags.removeAll(tags);
	}

	default void setTags(Collection<String> tags) {
		vl$self().tags.clear();
		vl$self().tags.addAll(tags);
	}

	default boolean isStaff() {
		return CommonGameEngine.INSTANCE.isPlayerStaff(vl$self().entityTags(), EntityUtils.getGameMode(vl$self()));
	}

	default boolean isStaffOrTalent() {
		return CommonGameEngine.INSTANCE.isPlayerStaffOrTalent(vl$self().entityTags(), EntityUtils.getGameMode(vl$self()));
	}
}
