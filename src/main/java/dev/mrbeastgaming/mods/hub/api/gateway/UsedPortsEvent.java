package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.mrbeastgaming.mods.hub.api.data.HubUsedPort;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

import java.util.List;

public class UsedPortsEvent extends Event {
	private final MinecraftServer server;
	private final List<HubUsedPort> usedPorts;

	public UsedPortsEvent(MinecraftServer server, List<HubUsedPort> usedPorts) {
		this.server = server;
		this.usedPorts = usedPorts;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public List<HubUsedPort> getUsedPorts() {
		return usedPorts;
	}
}
