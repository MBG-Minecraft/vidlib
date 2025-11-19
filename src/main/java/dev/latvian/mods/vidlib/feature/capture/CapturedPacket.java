package dev.latvian.mods.vidlib.feature.capture;

import net.minecraft.network.protocol.Packet;

public record CapturedPacket(long gameTime, Packet<?> packet, boolean config) {
}
