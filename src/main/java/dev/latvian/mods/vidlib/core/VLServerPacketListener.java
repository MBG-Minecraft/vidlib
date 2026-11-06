package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.ServerWorldSessionData;

public interface VLServerPacketListener extends VLPacketListener {
	@Override
	default ServerWorldSessionData vl$getWorldData() {
		throw new NoMixinException(this);
	}

	default void vl$setWorldData(ServerWorldSessionData data) {
		throw new NoMixinException(this);
	}
}
