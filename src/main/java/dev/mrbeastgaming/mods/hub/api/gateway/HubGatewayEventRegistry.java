package dev.mrbeastgaming.mods.hub.api.gateway;

import net.minecraft.util.thread.ReentrantBlockableEventLoop;

import java.util.function.BiConsumer;

public interface HubGatewayEventRegistry<M extends ReentrantBlockableEventLoop<?>> {
	void register(String event, BiConsumer<M, HubGatewayEvent> callback);

	void registerSynced(String event, BiConsumer<M, HubGatewayEvent> callback);
}
