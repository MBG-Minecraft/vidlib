package dev.mrbeastgaming.mods.hub.client;

import com.mojang.blaze3d.Blaze3D;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

public class LinkHubUserScreen extends ConfirmScreen {
	public static void open(Minecraft mc) {
		if (!(mc.gui.screen() instanceof LinkHubUserScreen)) {
			mc.gui.setScreen(new LinkHubUserScreen());
		}
	}

	private static void handle(boolean confirm) {
		var mc = Minecraft.getInstance();

		if (confirm) {
			mc.gui.setScreen(new LinkHubUserWaitingScreen());
			var port = HubLocalServer.getWebServer();
			Blaze3D.openUri(HubAPI.URI_BASE.resolve("/minecraft/link/" + port));
		} else if (HubUserCapabilities.get().resolveRequireLink()) {
			mc.stop();
		} else {
			mc.gui.setScreen(null);
		}
	}

	public LinkHubUserScreen() {
		super(
			LinkHubUserScreen::handle,
			Component.literal("MrBeast Gaming Hub Profile Linking"),
			Component.literal("You can only participate MrBeast Gaming events if you've linked your Minecraft profile"),
			Component.literal("Link"),
			Component.literal(HubUserCapabilities.get().resolveRequireLink() ? "Quit" : "Skip")
		);
	}
}
