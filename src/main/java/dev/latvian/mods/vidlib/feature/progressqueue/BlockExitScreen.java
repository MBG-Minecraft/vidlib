package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.mrbeastgaming.mods.hub.client.UnskippableScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class BlockExitScreen extends UnskippableScreen {
	public static boolean bypass = false;

	public static boolean stop(Minecraft mc) {
		if (bypass) {
			return false;
		} else if (mc.screen instanceof BlockExitScreen) {
			return true;
		}

		if (mc.level != null) {
			mc.vl$exitToTitle();
			return true;
		} else if (ProgressQueue.isBlockingExit()) {
			mc.setScreen(new BlockExitScreen());
			return true;
		}

		return false;
	}

	public static boolean cancelExit(Minecraft mc) {
		return !bypass && (mc.screen instanceof BlockExitScreen || mc.level != null || ProgressQueue.isBlockingExit());
	}

	public Button button;
	public final long openTime;

	public BlockExitScreen() {
		super(
			Component.literal("Still uploading files!").withStyle(ChatFormatting.RED),
			Component.literal("Please wait until all upload tasks are complete")
		);

		this.openTime = System.currentTimeMillis();
	}

	@Override
	protected void init() {
		super.init();
		int k = 150;
		button = this.addRenderableWidget(Button.builder(Component.literal("Quit Now").withStyle(ChatFormatting.YELLOW), this::buttonClicked).bounds((this.width - k) / 2, this.height - 60, k, 20).build());
		button.active = true;
	}

	@Override
	public void tick() {
		super.tick();

		if (!ProgressQueue.isBlockingExit()) {
			bypass = true;
			minecraft.stop();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public void buttonClicked(Button button) {
		bypass = true;
		minecraft.stop();
	}
}
