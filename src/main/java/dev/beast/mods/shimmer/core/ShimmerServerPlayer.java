package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public interface ShimmerServerPlayer extends ShimmerPlayer {
	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToPlayer((ServerPlayer) this, packet);
	}
}
