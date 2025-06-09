package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.gl.GLDebugLog;

public interface VLGpuBuffer {
	default int vl$getHandle() {
		return -1;
	}

	default void setLabel(String label) {
		GLDebugLog.bufferLabel(vl$getHandle(), label);
	}
}
