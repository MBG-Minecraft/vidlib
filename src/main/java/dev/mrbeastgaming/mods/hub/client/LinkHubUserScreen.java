package dev.mrbeastgaming.mods.hub.client;

import dev.mrbeastgaming.mods.hub.api.HubAPI;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

public class LinkHubUserScreen extends ConfirmScreen {
	public static void open(Minecraft mc) {
		if (!(mc.screen instanceof LinkHubUserScreen)) {
			mc.pushGuiLayer(new LinkHubUserScreen());
		}
	}

	private static void handle(boolean confirm) {
		var mc = Minecraft.getInstance();

		if (confirm) {
			mc.popGuiLayer();
			mc.pushGuiLayer(new LinkHubUserWaitingScreen());
			var port = HubLocalServer.getWebServer();
			Util.getPlatform().openUri(HubAPI.URI_BASE.resolve("/desktop/link/" + port));
		} else {
			mc.stop();
		}
	}

	public LinkHubUserScreen() {
		super(
			LinkHubUserScreen::handle,
			Component.literal("MrBeast Gaming Hub Profile Linking"),
			Component.literal("You can only participate MrBeast Gaming events if you've linked your Minecraft profile"),
			Component.literal("Link"),
			Component.literal("Quit")
		);
	}
}
