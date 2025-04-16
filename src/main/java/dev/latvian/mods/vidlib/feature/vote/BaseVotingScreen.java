package dev.latvian.mods.vidlib.feature.vote;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

public abstract class BaseVotingScreen extends Screen {
	public final Component subtitle;
	public final CompoundTag extraData;
	public Button submitButton;
	public int selected = -1;
	public boolean waiting;

	protected BaseVotingScreen(CompoundTag extraData, Component title, Component subtitle) {
		super(title);
		this.extraData = extraData;
		this.subtitle = subtitle;
	}

	@Override
	protected void init() {
		super.init();

		submitButton = addRenderableWidget(Button.builder(Component.literal("Submit"), button -> {
			button.active = false;
			waiting = true;

			if (postEvent()) {
				minecraft.player.vl$closeScreen();
			} else {
				sendPayload();
				button.setMessage(Component.literal("Submitted!"));
			}
		}).bounds((width - 150) / 2, height - 40, 150, 20).build());

		submitButton.active = selected != -1 && !waiting;
	}

	public boolean postEvent() {
		return NeoForge.EVENT_BUS.post(new PlayerVotedEvent(minecraft.player, extraData, selected)).isCanceled();
	}

	public void sendPayload() {
		minecraft.c2s(new PlayerVotedPayload(extraData, selected));
	}

	@Override
	public void render(GuiGraphics graphics, int mx, int my, float delta) {
		super.render(graphics, mx, my, delta);

		graphics.pose().pushPose();
		graphics.pose().translate(width / 2F, 30F, 0F);
		graphics.pose().scale(2F, 2F, 1F);
		graphics.drawString(font, title, -font.width(title) / 2, -4, 0xFFFFFFFF, true);
		graphics.pose().popPose();

		var sub = font.split(subtitle, width - 40);

		for (int i = 0; i < sub.size(); i++) {
			graphics.drawString(font, sub.get(i), (width - font.width(sub.get(i))) / 2, 45 + i * 9, 0xFFFFFFFF, true);
		}
	}

	public void pressed(int button) {
		if (!waiting) {
			selected = button;
			submitButton.active = true;
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return hasShiftDown() && minecraft.isLocalServer();
	}
}
