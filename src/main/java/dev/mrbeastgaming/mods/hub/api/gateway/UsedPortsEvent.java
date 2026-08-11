package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.mrbeastgaming.mods.hub.api.UsedPort;
import net.neoforged.bus.api.Event;

import java.util.List;

public class UsedPortsEvent extends Event {
	private final List<UsedPort> usedPorts;

	public UsedPortsEvent(List<UsedPort> usedPorts) {
		this.usedPorts = usedPorts;
	}

	public List<UsedPort> getUsedPorts() {
		return usedPorts;
	}
}
