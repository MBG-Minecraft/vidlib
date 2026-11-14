package dev.latvian.mods.vidlib.feature.canvas;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;

public class CanvasRegistryEvent extends Event implements IModBusEvent {
	private final Consumer<Canvas> callback;

	public CanvasRegistryEvent(Consumer<Canvas> callback) {
		this.callback = callback;
	}

	public void register(Canvas canvas) {
		callback.accept(canvas);
	}
}
