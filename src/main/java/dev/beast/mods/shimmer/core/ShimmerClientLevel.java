package dev.beast.mods.shimmer.core;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public interface ShimmerClientLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToServer(packet);
	}
}
