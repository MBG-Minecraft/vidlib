package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.mrbeastgaming.mods.hub.api.project.UsedPort;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

import java.util.List;

public class UsedPortsEvent extends Event {
	private final MinecraftServer server;
	private final List<UsedPort> usedPorts;

	public UsedPortsEvent(MinecraftServer server, List<UsedPort> usedPorts) {
		this.server = server;
		this.usedPorts = usedPorts;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public List<UsedPort> getUsedPorts() {
		return usedPorts;
	}
}
