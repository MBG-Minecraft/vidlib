package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.WorldSessionData;
import dev.latvian.mods.vidlib.feature.session.LoginData;

public interface VLPacketListener {
	default WorldSessionData vl$getWorldData() {
		throw new NoMixinException(this);
	}

	default void vl$addLoginData(LoginData data) {
		throw new UnsupportedOperationException("Attempted to add login data outside of the configuration phase!");
	}
}
