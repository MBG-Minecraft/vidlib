package dev.beast.mods.shimmer.core;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public interface ShimmerMinecraftClient extends ShimmerMinecraftEnvironment {
	default Minecraft shimmer$self() {
		return (Minecraft) this;
	}

	@Override
	default List<? extends Player> shimmer$getPlayers() {
		var player = shimmer$self().player;
		return player == null ? List.of() : List.of(player);
	}

	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToServer(packet);
	}
}
