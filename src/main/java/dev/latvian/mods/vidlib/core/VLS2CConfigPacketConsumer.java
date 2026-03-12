package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface VLS2CConfigPacketConsumer {
	void s2c(@Nullable Packet<? super ClientConfigurationPacketListener> packet);

	default void s2c(CustomPacketPayload packet) {
		s2c(new ClientboundCustomPayloadPacket(packet));
	}

	default void s2c(SimplePacketPayload packet) {
		s2c(packet.toS2CPacket(0L));
	}
}
