package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.ClientSessionData;

public interface VLClientPlayer extends VLPlayer {
	@Override
	default ClientSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}
}
