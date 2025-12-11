package dev.latvian.mods.vidlib.core;

import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ConfigurationTask;

public interface VLServerConfigPacketListener extends VLServerPacketListener {
	default void vl$transfer(VLServerPlayPacketListener play, PacketListener packetListener) {
		throw new NoMixinException(this);
	}

	default void vl$finishTask(ConfigurationTask.Type type) {
		throw new NoMixinException(this);
	}
}
