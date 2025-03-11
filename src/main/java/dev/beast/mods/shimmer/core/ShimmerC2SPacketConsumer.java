package dev.beast.mods.shimmer.core;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

import java.util.List;

public interface ShimmerC2SPacketConsumer {
	void c2s(Packet<? super ServerGamePacketListener> packet);

	default void c2s(CustomPacketPayload packet) {
		c2s(new ServerboundCustomPayloadPacket(packet));
	}

	default void c2s(List<Packet<? super ServerGamePacketListener>> packets) {
		for (var packet : packets) {
			c2s(packet);
		}
	}
}
