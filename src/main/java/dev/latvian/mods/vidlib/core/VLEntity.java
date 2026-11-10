package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.vidlib.feature.VLFeatures;
import dev.latvian.mods.vidlib.feature.entity.ForceEntityVelocityPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VLEntity extends VLLevelContainer, PlayerActionHandler {
	default Entity vl$self() {
		return (Entity) this;
	}

	@Override
	default Level vl$level() {
		return vl$self().level();
	}

	default void forceSetVelocity(Vec3 velocity) {
		var e = vl$self();
		e.setDeltaMovement(velocity);

		if (!e.level().isClientSide()) {
			PlayerTarget.of(e.level()).s2c(new ForceEntityVelocityPayload(e.getId(), velocity));
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
		VLFeatures.of(e.level()).playGlobalSound(new PositionedSoundData(data, e, looping, stopImmediately), KNumberVariables.EMPTY);
	}

	default void playSound(SoundData data) {
		playSound(data, false, true);
	}

	default Vec3 getSoundSource(float delta) {
		return vl$self().getEyePosition(delta);
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

	default void replaySnapshot(S2CPacketConsumer packets) {
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
}
