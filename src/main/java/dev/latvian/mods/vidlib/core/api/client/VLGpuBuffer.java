package dev.latvian.mods.vidlib.core.api.client;

public interface VLGpuBuffer {
	default int vl$getHandle() {
		return -1;
	}
}
