package dev.latvian.mods.vidlib.core;

public interface VLGpuBuffer {
	default int vl$getHandle() {
		return -1;
	}
}
