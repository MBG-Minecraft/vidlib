package dev.beast.mods.shimmer.util;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface CustomPacketHandler {
	void send(CustomPacketPayload packet);
}
