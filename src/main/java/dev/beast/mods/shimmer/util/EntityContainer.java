package dev.beast.mods.shimmer.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface EntityContainer {
	default List<Entity> shimmer$getEntities() {
		return List.of();
	}

	default List<? extends Player> shimmer$getPlayers() {
		return List.of();
	}

	default void send(CustomPacketPayload packet) {
		var p = new ClientboundCustomPayloadPacket(packet);

		for (var player : shimmer$getPlayers()) {
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.connection.send(p);
			}
		}
	}

	default void tell(Component message) {
		for (var player : shimmer$getPlayers()) {
			player.sendSystemMessage(message);
		}
	}

	default void tell(String message) {
		tell(Component.literal(message));
	}

	default void status(Component message) {
		for (var player : shimmer$getPlayers()) {
			player.displayClientMessage(message, true);
		}
	}

	default void status(String message) {
		status(Component.literal(message));
	}
}
