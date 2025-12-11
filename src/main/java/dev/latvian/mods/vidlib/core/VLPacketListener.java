package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.LoginData;
import dev.latvian.mods.vidlib.feature.session.SessionData;

public interface VLPacketListener {
	default SessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	default void vl$addLoginData(LoginData data) {
		throw new UnsupportedOperationException("Attempted to add login data outside of the configuration phase!");
	}
}
