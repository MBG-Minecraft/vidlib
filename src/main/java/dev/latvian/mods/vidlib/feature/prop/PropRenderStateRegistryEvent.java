package dev.latvian.mods.vidlib.feature.prop;

import net.neoforged.bus.api.Event;

import java.util.function.BiConsumer;

public class PropRenderStateRegistryEvent extends Event {
	private final BiConsumer<PropType<?>, PropRenderStateFactory<?, ?>> callback;

	public PropRenderStateRegistryEvent(BiConsumer<PropType<?>, PropRenderStateFactory<?, ?>> callback) {
		this.callback = callback;
	}

	public <P extends Prop, S extends PropRenderState<P>> void register(PropType<P> type, PropRenderStateFactory<P, S> factory) {
		callback.accept(type, factory);
	}
}
