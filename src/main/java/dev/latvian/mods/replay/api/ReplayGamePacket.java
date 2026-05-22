package dev.latvian.mods.replay.api;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ReplayGamePacket(int replayTime, long gameTime, ResourceKey<Level> dimension, Packet<? super ClientGamePacketListener> packet) {
}
