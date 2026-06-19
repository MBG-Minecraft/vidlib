package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.server.MinecraftServer;

public interface VLServerPacketListener extends VLPacketListener {
	@Override
	default ServerSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	default void vl$sessionData(ServerSessionData data) {
		throw new NoMixinException(this);
	}

	default MinecraftServer vl$server() {
		throw new NoMixinException(this);
	}
}
