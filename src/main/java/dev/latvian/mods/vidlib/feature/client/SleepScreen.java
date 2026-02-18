package dev.latvian.mods.vidlib.feature.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class SleepScreen extends InBedChatScreen {
	@Override
	protected void init() {
		super.init();
		leaveBedButton.setY(height - 70);
	}

	@Override
	public void render(GuiGraphics graphics, int x, int y, float delta) {
		this.leaveBedButton.render(graphics, x, y, delta);
	}

	@Override
	public boolean charTyped(char p_263331_, int p_263427_) {
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			this.sendWakeUp();
		}

		return true;
	}

	private void sendWakeUp() {
		minecraft.player.connection.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
	}
}
