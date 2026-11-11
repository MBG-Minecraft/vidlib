package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.klib.core.NoMixinException;

public interface VLDirectStateAccess {
	default int vl$createFrameBufferObject() {
		throw new NoMixinException(this);
	}
}
