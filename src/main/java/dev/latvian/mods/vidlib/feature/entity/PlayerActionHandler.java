package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

public interface PlayerActionHandler {
	static boolean handle(Player player, PlayerActionType action, boolean call) {
		if (player.getControlledVehicle() instanceof PlayerActionHandler handler && handler.handlePlayerAction(player, action, call)) {
			return true;
		}

		var mainHandItem = player.getMainHandItem();

		if (VidLibTool.of(mainHandItem) instanceof PlayerActionHandler handler && handler.handlePlayerAction(player, action, call)) {
			return true;
		}

		return mainHandItem.getItem() instanceof PlayerActionHandler handler && handler.handlePlayerAction(player, action, call);
	}

	default Set<PlayerActionType> getHandledPlayerActions() {
		return Set.of();
	}

	@ApiStatus.Internal
	default boolean handlePlayerAction(Player player, PlayerActionType action, boolean call) {
		if (getHandledPlayerActions().contains(action)) {
			if (call) {
				if (player instanceof ServerPlayer serverPlayer) {
					onPlayerAction(serverPlayer, action);
				} else if (!onClientPlayerAction(player, action)) {
					player.level().c2s(new PlayerActionRequestPayload(action));
				}
			}

			return true;
		}

		return false;
	}

	default boolean onClientPlayerAction(Player player, PlayerActionType action) {
		return false;
	}

	default void onPlayerAction(ServerPlayer player, PlayerActionType action) {
		VidLib.LOGGER.info("Unhandled Pilot Action from %s: %s".formatted(player.getScoreboardName(), action.getSerializedName()));
	}
}
