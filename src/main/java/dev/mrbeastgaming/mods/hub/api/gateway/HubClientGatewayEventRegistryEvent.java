package dev.mrbeastgaming.mods.hub.api.gateway;

import net.minecraft.client.Minecraft;

public class HubClientGatewayEventRegistryEvent extends HubGatewayEventRegistryEvent<Minecraft> {
	public HubClientGatewayEventRegistryEvent(HubGatewayEventRegistry<Minecraft> registry) {
		super(registry);
	}
}
