package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public interface ShimmerLocalPlayer extends ShimmerPlayer {
	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToServer(packet);
	}
}
