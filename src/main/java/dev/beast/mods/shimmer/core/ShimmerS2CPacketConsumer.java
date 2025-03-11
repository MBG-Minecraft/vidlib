package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;

import java.util.List;

public interface ShimmerS2CPacketConsumer {
	void s2c(Packet<? super ClientGamePacketListener> packet);

	default void s2c(CustomPacketPayload packet) {
		s2c(new ClientboundCustomPayloadPacket(packet));
	}

	default void s2c(List<Packet<? super ClientGamePacketListener>> packets) {
		if (!packets.isEmpty()) {
			s2c(new ClientboundBundlePacket(packets));
		}
	}
}
