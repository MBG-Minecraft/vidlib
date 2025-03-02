package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public interface ShimmerClientLevel extends ShimmerLevel {
	@Override
	default ShimmerMinecraftEnvironment shimmer$getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	@Nullable
	default ActiveZones shimmer$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.shimmer$sessionData().filteredZones;
	}

	@Override
	default void send(CustomPacketPayload packet) {
		PacketDistributor.sendToServer(packet);
	}
}
