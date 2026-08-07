package dev.mrbeastgaming.mods.hub.api.gateway;

import net.minecraft.server.MinecraftServer;

public class HubServerGatewayEventRegistryEvent extends HubGatewayEventRegistryEvent<MinecraftServer> {
	public HubServerGatewayEventRegistryEvent(HubGatewayEventRegistry<MinecraftServer> registry) {
		super(registry);
	}
}
