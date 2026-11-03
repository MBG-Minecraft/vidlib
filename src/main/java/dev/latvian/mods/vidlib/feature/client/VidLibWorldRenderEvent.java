package dev.latvian.mods.vidlib.feature.client;

import net.neoforged.bus.api.Event;

public class VidLibWorldRenderEvent extends Event {
	private final VidLibExtractContext extractContext;

	public VidLibWorldRenderEvent(VidLibExtractContext extractContext) {
		this.extractContext = extractContext;
	}

	public VidLibExtractContext getExtractContext() {
		return extractContext;
	}
}
