package dev.mrbeastgaming.mods.hub.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;

public class OtherServerHeader extends ServerSelectionList.Entry {
	private static final Component OTHER_SERVERS = Component.literal("Other Servers");

	private final Minecraft minecraft = Minecraft.getInstance();

	@Override
	public void render(
		GuiGraphics graphics,
		int index,
		int top,
		int left,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovering,
		float partialTick
	) {
		int i = top + height / 2 - 9 / 2;
		graphics.drawString(
			this.minecraft.font,
			OTHER_SERVERS,
			this.minecraft.screen.width / 2 - this.minecraft.font.width(OTHER_SERVERS) / 2,
			i,
			-1
		);
	}

	@Override
	public Component getNarration() {
		return OTHER_SERVERS;
	}
}
