package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.util.MessageConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class WorldSyncScreen extends Screen {
	public WorldSyncReadThread thread;
	public Button createWorldButton, cancelButton;

	public final List<ProgressingText> text;
	public long progress;
	public long maxProgress;
	private boolean threadStarted;

	public WorldSyncScreen() {
		super(Component.empty());
		text = new ArrayList<>();
		progress = 0L;
		maxProgress = 0L;
		threadStarted = false;
	}

	@Override
	protected void init() {
		super.init();
		boolean prevCreateWorldActive = createWorldButton != null && createWorldButton.active;
		boolean prevCancelActive = cancelButton == null || cancelButton.active;
		int y = height - 24;
		createWorldButton = addRenderableWidget(Button.builder(CommonComponents.GUI_PROCEED, this::done).bounds(this.width / 2 - 155, y, 150, 20).build());
		cancelButton = addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, this::cancelSync).bounds(this.width / 2 - 155 + 160, y, 150, 20).build());
		createWorldButton.active = prevCreateWorldActive;
		cancelButton.active = prevCancelActive;
	}

	public void startThread() {
		if (!threadStarted) {
			if (!text.isEmpty()) {
				text.get(0).setText("Indexing files... Done!");
			}

			threadStarted = true;
			thread.start();
		}
	}

	private void done(Button button) {
		try {
			WorldSyncCommands.create(MessageConsumer.ofPlayer(minecraft.player), thread.serverPath, thread.serverName);
			onClose();

		} catch (Exception ex) {
			ex.printStackTrace();
			ProgressingText t = new ProgressingText();
			t.setText("Failed to create world: " + ex).red();
			text.add(t);
		}
	}

	private void cancelSync(Button button) {
		button.active = false;
		thread.cancel();
		onClose();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		int textY = height - 30 - (text.size() * 12);

		for (int i = 0; i < text.size(); i++) {
			var t = text.get(i);

			int y = textY + i * 12;

			if (y >= 10) {
				graphics.drawString(font, t.text, 10, y, t.color);
				renderProgressBar(graphics, width - 50, y, 40, 11, t.progress, t.maxProgress);
			}
		}

		renderProgressBar(graphics, 0, 2, width - 2, 13, progress, maxProgress);
	}

	private void renderProgressBar(GuiGraphics graphics, int x, int y, int w, int h, long progress, long maxProgress) {
		if (progress <= 0L || maxProgress <= 0L) {
			return;
		}

		graphics.fillGradient(x, y, x + w, y + h, 0xFF808080, 0xFF808080);

		if (progress >= maxProgress) {
			graphics.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFFFFFFF, 0xFFFFFFFF);
		} else {
			graphics.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF000000, 0xFF000000);

			int rw = Mth.ceil(((double) progress / (maxProgress + 1D) * (w - 2D)));

			if (rw > 0) {
				graphics.fillGradient(x + 1, y + 1, x + rw, y + h - 1, 0xFFFFFFFF, 0xFFFFFFFF);
			}
		}

		String s = String.format("%.02f%%", progress * 100D / (double) maxProgress);

		if (s.endsWith(".00%")) {
			s = s.substring(0, s.length() - 4) + "%";
		}

		graphics.drawString(font, s, x + (w - font.width(s)) / 2F, Mth.ceil(y + (h - 8F) / 2F), 0xFF808080, false);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return thread.cancelled;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		/*
		if (keyCode == 256) {
			cancelSync(cancelButton);
			return true;
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		*/

		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
