package dev.latvian.mods.replay.api;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;

public record ReplayConfigPacket(Packet<? super ClientConfigurationPacketListener> packet) {
}
