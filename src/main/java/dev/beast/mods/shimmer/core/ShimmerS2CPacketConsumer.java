package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public interface ShimmerS2CPacketConsumer {
	void s2c(@Nullable Packet<? super ClientGamePacketListener> packet);

	default void s2c(CustomPacketPayload packet) {
		s2c(new ClientboundCustomPayloadPacket(packet));
	}
}
