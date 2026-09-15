package dev.mrbeastgaming.mods.hub.api.gateway;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

import java.util.List;

public class AvailableWorldsEvent extends Event {
	private final MinecraftServer server;
	private final List<HubWorldDirectory> worlds;

	public AvailableWorldsEvent(MinecraftServer server, List<HubWorldDirectory> worlds) {
		this.server = server;
		this.worlds = worlds;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public List<HubWorldDirectory> getWorlds() {
		return worlds;
	}
}
