package dev.latvian.mods.vidlib.feature.session;

import net.minecraft.network.protocol.game.ServerGamePacketListener;

public interface LoginData {
	void transfer(ServerGamePacketListener connection, ServerSessionData data);
}
