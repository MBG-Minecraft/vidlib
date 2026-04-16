package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.mrbeastgaming.mods.hub.api.gateway.event.HubGatewayEvent;
import net.neoforged.bus.api.Event;

import java.util.Map;
import java.util.function.Consumer;

public class HubGatewayEventRegistryEvent extends Event {
	private final Map<String, Consumer<HubGatewayEvent>> map;

	public HubGatewayEventRegistryEvent(Map<String, Consumer<HubGatewayEvent>> map) {
		this.map = map;
	}

	public void register(String method, Consumer<HubGatewayEvent> callback) {
		map.put(method, callback);
	}
}
