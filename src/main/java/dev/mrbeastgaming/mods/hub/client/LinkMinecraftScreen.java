package dev.mrbeastgaming.mods.hub.client;

import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubClientSessionData;
import dev.mrbeastgaming.mods.hub.api.HubMinecraftProfileData;
import dev.mrbeastgaming.mods.hub.api.HubUserCapabilities;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LinkMinecraftScreen extends ConfirmScreen {
	public static void handle(Minecraft mc, boolean retry) {
		if (retry) {
			if (mc.screen instanceof LinkMinecraftScreen) {
				mc.popGuiLayer();
			}

			var screen = new LinkMinecraftScreen();
			mc.pushGuiLayer(screen);

			Util.ioPool().execute(() -> {
				var data = loadHubMinecraftProfile(mc);

				if (data != null) {
					mc.execute(mc::popGuiLayer);
				} else {
					mc.execute(() -> {
						mc.toast(Component.literal("Error!"), Component.literal("Couldn't connect to the API"));
						screen.setButtonsEnabled(true);
					});
				}
			});
		} else if (HubUserCapabilities.CURRENT.resolveRequireLink()) {
			mc.stop();
		} else {
			mc.popGuiLayer();
		}
	}

	@Nullable
	private static HubMinecraftProfileData.LinkData loadHubMinecraftProfile(Minecraft mc) {
		HubMinecraftProfileData.LinkData data = null;
		var serverId = HubClientSessionData.AUTH_SERVER_ID;

		if (!serverId.isEmpty()) {
			try {
				VidLib.LOGGER.info("Linking Minecraft " + mc.getUser().getName() + " @ " + serverId);
				mc.services().sessionService().joinServer(mc.getUser().getProfileId(), mc.getUser().getAccessToken(), serverId);
				data = HubAPI.apiMinecraftLink(mc.getUser().getName());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		HubMinecraftProfileData.SELF = data == null ? null : data.profile();
		HubMinecraftProfileData.TOKEN = data == null ? null : data.token();
		return data;
	}

	public boolean buttonsEnabled;
	private final List<Button> buttons;

	public LinkMinecraftScreen() {
		super(
			b -> LinkMinecraftScreen.handle(Minecraft.getInstance(), b),
			Component.literal("MrBeast Gaming Hub Profile Linking"),
			Component.literal("Connecting to API..."),
			Component.literal("Retry"),
			Component.literal(HubUserCapabilities.CURRENT.resolveRequireLink() ? "Quit" : "Skip")
		);

		this.buttons = new ArrayList<>();
		this.buttonsEnabled = false;
	}

	@Override
	protected void init() {
		buttons.clear();
		super.init();
		if (yesButton != null) {
			buttons.add(yesButton);
		}
		if (noButton != null) {
			buttons.add(noButton);
		}
		setButtonsEnabled(buttonsEnabled);
	}

	public void setButtonsEnabled(boolean value) {
		buttonsEnabled = value;

		for (var button : buttons) {
			button.active = value;
		}
	}
}
