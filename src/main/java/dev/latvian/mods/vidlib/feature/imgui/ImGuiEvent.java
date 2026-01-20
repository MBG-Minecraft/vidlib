package dev.latvian.mods.vidlib.feature.imgui;

import net.neoforged.bus.api.Event;

public class ImGuiEvent extends Event {
	private final ImGraphics graphics;

	public ImGuiEvent(ImGraphics graphics) {
		this.graphics = graphics;
	}

	public ImGraphics getGraphics() {
		return graphics;
	}
}
