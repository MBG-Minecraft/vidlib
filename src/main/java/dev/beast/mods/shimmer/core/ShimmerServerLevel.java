package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public interface ShimmerServerLevel extends ShimmerLevel {
	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToPlayersInDimension((ServerLevel) this, packet);
	}
}
