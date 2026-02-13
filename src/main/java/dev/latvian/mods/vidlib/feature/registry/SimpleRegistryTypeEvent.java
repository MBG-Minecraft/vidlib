package dev.latvian.mods.vidlib.feature.registry;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;

public class SimpleRegistryTypeEvent<T> extends Event implements IModBusEvent {
	private final SimpleRegistryCollector<T> callback;

	public SimpleRegistryTypeEvent(SimpleRegistryCollector<T> callback) {
		this.callback = callback;
	}

	public void register(SimpleRegistryType<? extends T> type) {
		callback.register(type);
	}

	public void registerAll(Consumer<SimpleRegistryCollector<T>> consumer) {
		consumer.accept(callback);
	}
}
