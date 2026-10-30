package dev.mrbeastgaming.mods.hub.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;

public class OtherServerHeader extends ServerSelectionList.Entry {
	private static final Component OTHER_SERVERS = Component.literal("Other Servers");

	private final Minecraft minecraft = Minecraft.getInstance();

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		graphics.text(
			this.minecraft.font,
			OTHER_SERVERS,
			this.getContentXMiddle() - this.minecraft.font.width(OTHER_SERVERS) / 2,
			this.getContentY(),
			-1
		);
	}

	@Override
	public Component getNarration() {
		return OTHER_SERVERS;
	}

	@Override
	protected boolean matches(ServerSelectionList.Entry other) {
		return other instanceof OtherServerHeader;
	}

	@Override
	public void join() {
	}
}
