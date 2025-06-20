package dev.latvian.mods.vidlib.feature.imgui;

import it.unimi.dsi.fastutil.chars.CharConsumer;
import net.neoforged.bus.api.Event;

public class ImIconsEvent extends Event {
	private final CharConsumer callback;

	ImIconsEvent(CharConsumer callback) {
		this.callback = callback;
	}

	public void add(char icon) {
		callback.accept(icon);
	}
}
