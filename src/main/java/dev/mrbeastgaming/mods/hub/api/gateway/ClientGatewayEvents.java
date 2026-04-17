package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.api.HubGameServerData;
import dev.mrbeastgaming.mods.hub.api.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.HubUserData;
import dev.mrbeastgaming.mods.hub.api.HubUserFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class ClientGatewayEvents {
	public static void registerSynced(HubGatewayEventRegistryEvent event, String method, BiConsumer<Minecraft, HubGatewayEvent> callback) {
		event.register(method, e -> Minecraft.getInstance().execute(() -> callback.accept(Minecraft.getInstance(), e)));
	}

	@SubscribeEvent
	public static void hubGatewayEventRegistry(HubGatewayEventRegistryEvent event) {
		event.register("ping", ClientGatewayEvents::ping);
		registerSynced(event, "request_restart", ClientGatewayEvents::requestRestart);
		registerSynced(event, "display_toast", ClientGatewayEvents::displayToast);
		event.register("user_updated", ClientGatewayEvents::userUpdated);
		event.register("flags_updated", ClientGatewayEvents::flagsUpdated);
		event.register("capabilities_updated", ClientGatewayEvents::capabilitiesUpdated);
		event.register("server_list_updated", ClientGatewayEvents::serverListUpdated);
		// TODO: edit options
		// TODO: save replay
		// TODO: save voice recording
		// TODO: open url
	}

	private static void ping(HubGatewayEvent event) {
		event.respond(new JsonPrimitive("pong"));
	}

	private static void requestRestart(Minecraft mc, HubGatewayEvent event) {
		if (mc.level != null) {
			mc.vl$exitToTitle();
		}

		// Display GUI
		mc.stop();
	}

	private static void displayToast(Minecraft mc, HubGatewayEvent event) {
		var params = event.paramsObject();
		var title = params.get("title").getAsString();
		var subtitle = params.has("subtitle") ? params.get("subtitle").getAsString() : "";
		mc.toast(Component.literal(title), subtitle.isEmpty() ? Component.empty() : Component.literal(subtitle));
	}

	private static void userUpdated(HubGatewayEvent event) {
		var user = HubUserData.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
		HubUserData.KNOWN_USERS.put(user.id().raw(), user); // sync?

		var self = HubUserData.SELF;

		if (self != null && self.id().equals(user.id())) {
			HubUserData.SELF = user;
		}
	}

	private static void flagsUpdated(HubGatewayEvent event) {
		var self = HubUserData.SELF;

		if (self != null) {
			var flags = HubUserFlags.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
			HubUserData.SELF = self.withFlags(flags);
		}
	}

	private static void capabilitiesUpdated(HubGatewayEvent event) {
		HubUserCapabilities.CURRENT = event.params() == null ? HubUserCapabilities.DEFAULT : HubUserCapabilities.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
	}

	private static void serverListUpdated(HubGatewayEvent event) {
		HubGameServerData.CURRENT = event.params() == null ? List.of() : HubGameServerData.LIST_CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
	}
}
