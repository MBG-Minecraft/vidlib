package dev.mrbeastgaming.mods.hub.api.gateway;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLJoinMultiplayerScreen;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.HubProjectsResponse;
import dev.mrbeastgaming.mods.hub.api.data.HubGameServer;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.data.HubUserFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class ClientGatewayEvents {
	@SubscribeEvent
	public static void hubGatewayEventRegistry(HubClientGatewayEventRegistryEvent event) {
		var registry = event.getRegistry();
		registry.registerSynced("request_restart", ClientGatewayEvents::requestRestart);
		registry.registerSynced("display_toast", ClientGatewayEvents::displayToast);
		registry.register("user_updated", ClientGatewayEvents::userUpdated);
		registry.register("flags_updated", ClientGatewayEvents::flagsUpdated);
		registry.register("capabilities_updated", ClientGatewayEvents::capabilitiesUpdated);
		registry.register("server_list_updated", ClientGatewayEvents::serverListUpdated);
		registry.register("project_updated", ClientGatewayEvents::projectUpdated);
		// TODO: edit options
		// TODO: save replay
		// TODO: save voice recording
		// TODO: open url
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

	private static void userUpdated(Minecraft mc, HubGatewayEvent event) {
		var user = HubUser.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
		var self = HubClientSession.CURRENT.user;

		if (self != null && self.id().equals(user.id())) {
			HubClientSession.CURRENT.user = user;
		}
	}

	private static void flagsUpdated(Minecraft mc, HubGatewayEvent event) {
		var self = HubClientSession.CURRENT.user;

		if (self != null) {
			var flags = HubUserFlags.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
			HubClientSession.CURRENT.user = self.withFlags(flags);
		}
	}

	private static void capabilitiesUpdated(Minecraft mc, HubGatewayEvent event) {
		HubClientSession.CURRENT.capabilities = event.params() == null ? HubUserCapabilities.DEFAULT : HubUserCapabilities.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
	}

	private static void serverListUpdated(Minecraft mc, HubGatewayEvent event) {
		HubClientSession.CURRENT.servers = event.params() == null ? List.of() : HubGameServer.LIST_CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();

		mc.execute(() -> {
			if (mc.screen instanceof VLJoinMultiplayerScreen screen) {
				screen.vl$refresh();
			}
		});
	}

	private static void projectUpdated(Minecraft mc, HubGatewayEvent event) {
		var data = HubProject.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();
		HubProjectsResponse.ALL.forget();

		var project = HubClientSession.CURRENT.project;

		if (project != null && project.id().equals(data.id())) {
			HubClientSession.CURRENT.project = data;
		}
	}

	private static void cutRecording(Minecraft mc, HubGatewayEvent event) {
		ReplayAPI.getActive().cutRecording();
	}
}
