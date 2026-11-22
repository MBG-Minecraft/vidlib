package dev.latvian.mods.vidlib.feature.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VLVehicle {
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

	default void setPilotInput(Player player, int input) {
	}

	default void sortPassengers(List<Entity> passengers) {
	}

	default boolean hidePassengerCrosshair(Player player) {
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
