package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.client.GLDebugLog;

public interface VLGpuTexture {
	default int vl$getHandle() {
		return 0;
	}

	default void setLabel(String label) {
		GLDebugLog.textureLabel(vl$getHandle(), label);
	}
}
