package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.RemoteClientSessionData;

public interface VLRemotePlayer extends VLClientPlayer {
	@Override
	default RemoteClientSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}
}
