package dev.latvian.mods.vidlib.feature.session;

import net.minecraft.network.PacketListener;

public interface LoginData {
	void transfer(PacketListener packetListener);
}
