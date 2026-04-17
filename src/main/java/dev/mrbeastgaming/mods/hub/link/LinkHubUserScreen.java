package dev.mrbeastgaming.mods.hub.link;

import dev.mrbeastgaming.mods.hub.api.HubAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LinkHubUserScreen extends Screen {
	public static void open(Minecraft mc) {
		if (mc.screen instanceof LinkHubUserScreen || mc.screen instanceof ConfirmScreen) {
			return;
		}

		mc.pushGuiLayer(new ConfirmScreen(value -> {
			mc.popGuiLayer();

			if (value) {
				mc.pushGuiLayer(new LinkHubUserScreen());
				var port = HubLocalServer.getWebServer();
				Util.getPlatform().openUri(HubAPI.URI_BASE.resolve("/desktop/link/" + port));
			}
		},
			Component.literal("Link MrBeast Gaming Hub Profile").withStyle(ChatFormatting.YELLOW),
			Component.empty(),
			Component.literal("Link").withStyle(ChatFormatting.GREEN),
			Component.literal("Skip").withStyle(ChatFormatting.RED)
		));
	}

	public LinkHubUserScreen() {
		super(Component.literal("Link MrBeast Gaming Hub Profile"));
	}

	@Override
	protected void init() {
		// cancel button
		super.init();
	}

	@Override
	public void removed() {
		super.removed();
	}
}
