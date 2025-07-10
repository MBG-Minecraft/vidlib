package dev.latvian.mods.vidlib.feature.imgui;

import net.neoforged.bus.api.Event;

import java.util.function.Consumer;

public class ImIconsEvent extends Event {
	private final Consumer<ImIcon> callback;

	ImIconsEvent(Consumer<ImIcon> callback) {
		this.callback = callback;
	}

	public void add(ImIcon icon) {
		callback.accept(icon);
	}
}
