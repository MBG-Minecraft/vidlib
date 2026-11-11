package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;

public interface VLClientConfigPacketListener {
	default GlobalSessionData vl$getGlobalData() {
		throw new NoMixinException(this);
	}
}
