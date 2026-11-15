package dev.latvian.mods.vidlib.feature.progressqueue;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

public class BlockExitScreen extends ConfirmScreen {
	public static boolean bypass = false;

	public static boolean preventExit(Minecraft mc, boolean act) {
		if (bypass) {
			return false;
		} else if (mc.gui.screen() instanceof BlockExitScreen) {
			return true;
		}

		if (mc.level != null) {
			if (act) {
				mc.execute(mc::vl$exitToTitle);
			}

			return true;
		} else if (ProgressQueue.isBlockingExit()) {
			if (act) {
				mc.gui.setScreen(new BlockExitScreen());
			}

			return true;
		}

		return false;
	}

	public static boolean preventExit(Window window) {
		var mc = Minecraft.getInstance();
		return window == mc.getWindow() && preventExit(mc, true);
	}

	private static void handle(boolean confirm) {
		var mc = Minecraft.getInstance();

		if (confirm) {
			bypass = true;
			mc.stop();
		} else {
			mc.gui.setScreen(null);
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
