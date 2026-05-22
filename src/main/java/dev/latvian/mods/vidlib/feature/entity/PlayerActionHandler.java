package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

public interface PlayerActionHandler {
	static boolean handle(Player player, PlayerActionType action, boolean call) {
		if (player.getControlledVehicle() instanceof PlayerActionHandler handler && handler.handlePlayerAction(player, ItemStack.EMPTY, InteractionHand.MAIN_HAND, action, call)) {
			return true;
		}

		var mainHandItem = player.getMainHandItem();

		if (VidLibTool.of(mainHandItem) instanceof PlayerActionHandler handler && handler.handlePlayerAction(player, mainHandItem, InteractionHand.MAIN_HAND, action, call)) {
			return true;
		}

		return mainHandItem.getItem() instanceof PlayerActionHandler handler && handler.handlePlayerAction(player, mainHandItem, InteractionHand.MAIN_HAND, action, call);
	}

	default Set<PlayerActionType> getHandledPlayerActions() {
		return Set.of();
	}

	@ApiStatus.Internal
	default boolean handlePlayerAction(Player player, ItemStack item, InteractionHand hand, PlayerActionType action, boolean call) {
		if (getHandledPlayerActions().contains(action)) {
			if (call) {
				if (player instanceof ServerPlayer serverPlayer) {
					onPlayerAction(serverPlayer, item, hand, action);
				} else if (!onClientPlayerAction(player, item, hand, action)) {
					player.level().c2s(new PlayerActionRequestPayload(action, hand));
				}
			}

			return true;
		}

		return false;
	}

	default boolean onClientPlayerAction(Player player, ItemStack item, InteractionHand hand, PlayerActionType action) {
		return false;
	}

	default void onPlayerAction(ServerPlayer player, ItemStack item, InteractionHand hand, PlayerActionType action) {
		VidLib.LOGGER.info("Unhandled Pilot Action from %s: %s".formatted(player.getScoreboardName(), action.getSerializedName()));
	}
}
