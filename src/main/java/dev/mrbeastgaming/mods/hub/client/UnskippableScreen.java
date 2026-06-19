package dev.mrbeastgaming.mods.hub.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.concurrent.locks.ReentrantLock;

public abstract class UnskippableScreen extends Screen {
	private Component messageText;
	private MultiLineLabel message;
	private final ReentrantLock lock;

	public UnskippableScreen(Component title, Component messageText) {
		super(title);
		this.messageText = messageText;
		this.message = MultiLineLabel.EMPTY;
		this.lock = new ReentrantLock();
	}

	public void setMessage(Component messageText) {
		lock.lock();

		try {
			this.messageText = messageText;
			this.message = MultiLineLabel.create(this.font, messageText, this.width - 50);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Component getNarrationMessage() {
		lock.lock();

		try {
			return CommonComponents.joinForNarration(super.getNarrationMessage(), this.messageText);
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void init() {
		super.init();
		setMessage(messageText);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
		super.extractRenderState(graphics, mx, my, delta);
		graphics.centeredText(this.font, this.title, this.width / 2, 70, 0xFFFFFFFF);

		lock.lock();

		try {
			this.message.visitLines(TextAlignment.CENTER, this.width / 2, 90, 9, graphics.textRenderer());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
