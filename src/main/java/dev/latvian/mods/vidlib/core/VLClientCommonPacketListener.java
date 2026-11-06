package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.ClientWorldSessionData;

public interface VLClientCommonPacketListener extends VLPacketListener {
	@Override
	default ClientWorldSessionData vl$getWorldData() {
		throw new NoMixinException(this);
	}

	default void vl$setWorldData(ClientWorldSessionData data) {
		throw new NoMixinException(this);
	}
}
