package dev.mrbeastgaming.mods.hub.link;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class LinkHubUserWaitingScreen extends UnskippableScreen {
	public Button button;

	public LinkHubUserWaitingScreen() {
		super(
			Component.literal("MrBeast Gaming Hub Profile Linking"),
			Component.literal("Check your browser")
		);
	}

	@Override
	protected void init() {
		super.init();
		int k = 150;
		button = this.addRenderableWidget(Button.builder(Component.literal("Close").withStyle(ChatFormatting.GREEN), this::buttonClicked).bounds((this.width - k) / 2, this.height - 60, k, 20).build());
		button.active = false;
	}

	public void buttonClicked(Button button) {
		onClose();
	}
}
