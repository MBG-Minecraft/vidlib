package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.util.CustomPacketHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ShimmerLevel extends CustomPacketHandler {
	@Override
	default void send(CustomPacketPayload packet) {
	}
}
