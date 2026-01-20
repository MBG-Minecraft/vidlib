package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.ServerSessionData;

public interface VLServerPacketListener extends VLPacketListener {
	@Override
	default ServerSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	default void vl$sessionData(ServerSessionData data) {
		throw new NoMixinException(this);
	}
}
