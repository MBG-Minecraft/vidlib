package dev.latvian.mods.vidlib.core;

public interface VLDirectStateAccess {
	default int vl$createFrameBufferObject() {
		throw new NoMixinException(this);
	}
}
