package dev.mrbeastgaming.mods.hub.api.gateway;

import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.neoforged.bus.api.Event;

public abstract class HubGatewayEventRegistryEvent<M extends ReentrantBlockableEventLoop<?>> extends Event {
	private final HubGatewayEventRegistry<M> registry;

	public HubGatewayEventRegistryEvent(HubGatewayEventRegistry<M> registry) {
		this.registry = registry;
	}

	public HubGatewayEventRegistry<M> getRegistry() {
		return registry;
	}
}
