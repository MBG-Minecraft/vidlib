package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ShimmerClientPlayer extends ShimmerPlayer {
	@Override
	default void send(CustomPacketPayload packet) {
	}
}
