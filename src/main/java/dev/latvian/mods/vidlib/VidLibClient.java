package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.mrbeastgaming.mods.hub.HubConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.event.SyncClientFilesHubEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

public class VidLibClient {
	public static int levelTick = 0;

	public static void init() {
		MiscUtils.CLIENT_PLAYER.setValue(() -> Minecraft.getInstance().player);
		loadHub();
		Runtime.getRuntime().addShutdownHook(new Thread(HubClientGateway::stopGateway, "Stop-Client-Hub-Gateway"));
	}

	public static void loadHub() {
		HubConfig.load();
		HubAPI.CLIENT_GATEWAY.setValue(HubClientGateway::get);
		HubClientSession.load(Minecraft.getInstance());
	}

	public static void checkFileSync(boolean isFirstTime) {
		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> {
			var session = HubClientSession.CURRENT;

			if (session != null) {
				session.upload("Client Sync", uploads -> NeoForge.EVENT_BUS.post(new SyncClientFilesHubEvent(uploads, isFirstTime))).whenComplete((unused, throwable) -> {
					if (throwable != null) {
						VidLib.LOGGER.error("Failed to sync client files", throwable);
					}
				});
			}
		});
	}
}
