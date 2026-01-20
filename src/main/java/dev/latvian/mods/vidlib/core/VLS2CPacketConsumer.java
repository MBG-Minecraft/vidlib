package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public interface VLS2CPacketConsumer extends VLLevelContainer {
	void s2c(@Nullable Packet<? super ClientGamePacketListener> packet);

	default void s2c(CustomPacketPayload packet) {
		s2c(new ClientboundCustomPayloadPacket(packet));
	}

	default void s2c(SimplePacketPayload packet) {
		s2c(packet.toGameS2C(vl$level()));
	}
}
