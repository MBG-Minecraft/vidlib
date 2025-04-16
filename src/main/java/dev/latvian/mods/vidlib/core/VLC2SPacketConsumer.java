package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VLC2SPacketConsumer extends VLLevelContainer {
	void c2s(@Nullable Packet<? super ServerGamePacketListener> packet);

	default void c2s(CustomPacketPayload packet) {
		c2s(new ServerboundCustomPayloadPacket(packet));
	}

	default void c2s(List<Packet<? super ServerGamePacketListener>> packets) {
		for (var packet : packets) {
			c2s(packet);
		}
	}

	default void c2s(SimplePacketPayload packet) {
		c2s(packet.toC2S(vl$level()));
	}
}
