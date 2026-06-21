package dev.latvian.mods.vidlib.feature.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class SleepScreen extends InBedChatScreen {
	public SleepScreen() {
		super("", false);
	}

	@Override
	protected void init() {
		super.init();
		leaveBedButton.setY(height - 70);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float delta) {
		this.leaveBedButton.extractRenderState(graphics, x, y, delta);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == 256) {
			this.sendWakeUp();
		}

		return true;
	}

	private void sendWakeUp() {
		minecraft.player.connection.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
	}
}
