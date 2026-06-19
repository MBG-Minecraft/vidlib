package dev.latvian.mods.vidlib.core;

import com.mojang.blaze3d.opengl.DirectStateAccess;

public interface VLDirectStateAccess {
	default int vl$createFrameBufferObject() {
		throw new NoMixinException(this);
	}

	default DirectStateAccess vl$directStateAccess() {
		throw new NoMixinException(this);
	}
}
