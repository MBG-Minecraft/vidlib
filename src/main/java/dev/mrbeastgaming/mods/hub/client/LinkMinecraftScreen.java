package dev.mrbeastgaming.mods.hub.client;

import dev.latvian.mods.klib.util.Async;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.data.HubMinecraftProfile;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LinkMinecraftScreen extends ConfirmScreen {
	public static void handle(Minecraft mc, boolean retry) {
		if (retry) {
			if (mc.gui.screen() instanceof LinkMinecraftScreen) {
				mc.gui.setScreen(null);
			}

			var screen = new LinkMinecraftScreen();
			mc.gui.setScreen(screen);

			Async.EXECUTOR.execute(() -> {
				var data = loadHubMinecraftProfile(mc);

				if (data != null) {
					mc.execute(() -> mc.gui.setScreen(null));
				} else {
					mc.execute(() -> {
						mc.gui.toastManager().addToast(new net.minecraft.client.gui.components.toasts.SystemToast(
							net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
							Component.literal("Error!"),
							Component.literal("Couldn't connect to the API")
						));
						screen.setButtonsEnabled(true);
					});
				}
			});
		} else if (HubUserCapabilities.get().resolveRequireLink()) {
			mc.stop();
		} else {
			mc.gui.setScreen(null);
		}
	}

	@Nullable
	private static HubMinecraftProfile.LinkData loadHubMinecraftProfile(Minecraft mc) {
		var authServerId = HubClientSession.CURRENT.authServerId;

		HubMinecraftProfile.LinkData minecraftLink = null;

		if (!authServerId.isEmpty()) {
			try {
				VidLib.LOGGER.info("Linking Minecraft " + mc.getUser().getName() + " @ " + authServerId);
				mc.services().sessionService().joinServer(mc.getUser().getProfileId(), mc.getUser().getAccessToken(), authServerId);
				minecraftLink = HubAPI.MinecraftAPI.getLink(mc.getUser().getName());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		HubClientSession.CURRENT.minecraftLink = minecraftLink;
		return minecraftLink;
	}

	public boolean buttonsEnabled;
	private final List<Button> buttons;

	public LinkMinecraftScreen() {
		super(
			b -> LinkMinecraftScreen.handle(Minecraft.getInstance(), b),
			Component.literal("MrBeast Gaming Hub Profile Linking"),
			Component.literal("Connecting to API..."),
			Component.literal("Retry"),
			Component.literal(HubUserCapabilities.get().resolveRequireLink() ? "Quit" : "Skip")
		);

		this.buttons = new ArrayList<>();
		this.buttonsEnabled = false;
	}

	@Override
	protected void init() {
		buttons.clear();
		super.init();
		setButtonsEnabled(buttonsEnabled);
	}

	@Override
	protected void addButtons(LinearLayout buttonLayout) {
		super.addButtons(buttonLayout);

		if (this.yesButton != null) {
			buttons.add(this.yesButton);
		}

		if (this.noButton != null) {
			buttons.add(this.noButton);
		}
	}

	public void setButtonsEnabled(boolean value) {
		buttonsEnabled = value;

		for (var button : buttons) {
			button.active = value;
		}
	}
}
