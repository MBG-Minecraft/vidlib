package dev.latvian.mods.vidlib.feature.progressqueue;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

public class BlockExitScreen extends ConfirmScreen {
	public static boolean bypass = false;

	public static boolean preventExit(Minecraft mc, boolean act) {
		if (bypass) {
			return false;
		} else if (mc.screen instanceof BlockExitScreen) {
			return true;
		}

		if (mc.level != null) {
			if (act) {
				mc.execute(mc::vl$exitToTitle);
			}

			return true;
		} else if (ProgressQueue.isBlockingExit()) {
			if (act) {
				mc.pushGuiLayer(new BlockExitScreen());
			}

			return true;
		}

		return false;
	}

	public static boolean cancelExit(Minecraft mc) {
		return !bypass && (mc.screen instanceof BlockExitScreen || mc.level != null || ProgressQueue.isBlockingExit());
	}

	private static void handle(boolean confirm) {
		var mc = Minecraft.getInstance();

		if (confirm) {
			bypass = true;
			mc.stop();
		} else {
			mc.popGuiLayer();
		}
	}

	public BlockExitScreen() {
		super(
			BlockExitScreen::handle,
			Component.literal("Still uploading files!").withStyle(ChatFormatting.RED),
			Component.literal("Please wait until all upload tasks are complete"),
			Component.literal("Quit Now").withStyle(ChatFormatting.YELLOW),
			Component.literal("Cancel")
		);
	}

	@Override
	public void tick() {
		super.tick();

		if (!ProgressQueue.isBlockingExit()) {
			bypass = true;
			minecraft.stop();
		}
	}
}
