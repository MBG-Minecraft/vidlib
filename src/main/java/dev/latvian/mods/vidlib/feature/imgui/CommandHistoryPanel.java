package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;

public class CommandHistoryPanel extends AdminPanel {
	public static final CommandHistoryPanel INSTANCE = new CommandHistoryPanel();

	private CommandHistoryPanel() {
		super("command-history", "Command History");
	}

	@Override
	public void content() {
		var list = new ArrayList<>(Minecraft.getInstance().commandHistory().history());

		for (int i = list.size() - 1; i >= 0; i--) {
			var s = list.get(i);

			ImGui.text(s);

			if (ImGui.isItemHovered()) {
				ImGui.setTooltip("Click to Copy:\n\n" + s);
			}

			if (ImGui.isItemClicked()) {
				ImGui.setClipboardText(s);
			}
		}
	}
}
