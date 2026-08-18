package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.net.URI;

public class HubClientGateway extends HubCommonGateway<Minecraft> {
	public static HubClientGateway instance;

	@Nullable
	public static HubClientGateway startGateway(Minecraft mc, @Nullable URI uri) {
		stopGateway();
		var gateway = instance;

		if (gateway == null && uri != null) {
			gateway = new HubClientGateway(mc, uri);
			gateway.start();
			instance = gateway;
		}

		return gateway;
	}

	public static void stopGateway() {
		var gateway = instance;

		if (gateway != null) {
			gateway.stop();
			instance = null;
		}
	}

	public static void tickGateway() {
		var gateway = instance;

		if (gateway != null) {
			gateway.tick();
		}
	}

	public static void updateInfo(Minecraft mc, HubClientGateway gateway) {
		gateway.sendName(mc.getUser().getName());

		var server = mc.getCurrentServer();

		if (server != null) {
			gateway.sendStatus("Server - " + server.name);
		} else if (mc.level != null && PlatformHelper.CURRENT.isReplayLevel(mc.level)) {
			gateway.sendStatus("Replay Editor");
		} else if (mc.level != null) {
			gateway.sendStatus("Singleplayer");
		} else {
			gateway.sendStatus("Main Menu");
		}
	}

	public final Minecraft mc;

	public HubClientGateway(Minecraft mc, URI uri) {
		super(mc, uri);
		this.mc = mc;
	}

	@Override
	public void collectEventHandlers(HubGatewayEventRegistry<Minecraft> registry) {
		NeoForge.EVENT_BUS.post(new HubClientGatewayEventRegistryEvent(registry));
	}

	@Override
	public void onConnected() {
		updateInfo(main, this);
	}
}
