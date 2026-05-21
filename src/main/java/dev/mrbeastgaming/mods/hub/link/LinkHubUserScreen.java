package dev.mrbeastgaming.mods.hub.link;

import dev.mrbeastgaming.mods.hub.api.HubAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class LinkHubUserScreen extends Screen {
	public String error = "";
	private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;

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

	public void setError(String error) {
		this.error = error;
		this.multilineMessage = MultiLineLabel.create(this.font, Component.literal(error), this.width - 50);
	}

	@Override
	protected void init() {
		// cancel button
		super.init();
		this.multilineMessage = MultiLineLabel.create(this.font, Component.literal(error), this.width - 50);
	}

	@Override
	public void render(GuiGraphics graphics, int mx, int my, float delta) {
		super.render(graphics, mx, my, delta);
		this.multilineMessage.renderCentered(graphics, this.width / 2, this.messageTop());
	}

	private int messageHeight() {
		return this.multilineMessage.getLineCount() * 9;
	}

	private int titleTop() {
		int i = (this.height - this.messageHeight()) / 2;
		return Mth.clamp(i - 20 - 9, 10, 80);
	}

	private int messageTop() {
		return this.titleTop() + 20;
	}

	@Override
	public void removed() {
		super.removed();
	}
}
