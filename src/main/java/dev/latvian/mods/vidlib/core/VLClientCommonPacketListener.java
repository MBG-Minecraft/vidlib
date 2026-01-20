package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;

public interface VLClientCommonPacketListener extends VLPacketListener {
	@Override
	default LocalClientSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	default void vl$sessionData(LocalClientSessionData data) {
		throw new NoMixinException(this);
	}
}
