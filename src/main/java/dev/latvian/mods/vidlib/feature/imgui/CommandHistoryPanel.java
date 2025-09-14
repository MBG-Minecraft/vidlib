package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class CommandHistoryPanel extends AdminPanel {
	public static final CommandHistoryPanel INSTANCE = new CommandHistoryPanel();

	private EditBox input;
	private CommandSuggestions commandSuggestions;

	private CommandHistoryPanel() {
		super("command-history", "Command History");
	}

	@Override
	public void content(ImGraphics graphics) {
		var list = new ArrayList<>(graphics.mc.commandHistory().history());

		if (graphics.inGame && commandSuggestions == null) {
			var mc = Minecraft.getInstance();

			input = new EditBox(mc.font, 0, 0, Component.empty());
			input.setMaxLength(Integer.MAX_VALUE);

			this.commandSuggestions = new CommandSuggestions(
				mc,
				new ChatScreen(""),
				input,
				mc.font,
				false,
				false,
				1,
				10,
				true,
				0xD0000000
			);
		}

		var sink = new FormattedCharSinkPartBuilder();
		graphics.pushStack();
		graphics.setStyleVar(ImGuiStyleVar.ItemSpacing, 8F, 2F);

		for (int i = list.size() - 1; i >= 0; i--) {
			var s = list.get(i);

			if (graphics.inGame) {
				graphics.pushStack();
				graphics.setStyleVar(ImGuiStyleVar.FramePadding, 2F, 0F);

				if (ImGui.smallButton(ImIcons.PLAY + "###run-" + i)) {
					Minecraft.getInstance().runClientCommand(s);
				}

				graphics.popStack();

				ImGui.sameLine();

				input.setValue(s);
				commandSuggestions.updateCommandInfo();
				commandSuggestions.formatChat(s, 0).accept(sink);
				graphics.text(sink.build());
			} else {
				ImGui.text(s);
			}

			if (ImGui.isItemHovered()) {
				ImGui.beginTooltip();
				ImGui.text("Click to Copy:");
				ImGui.spacing();

				if (graphics.inGame) {
					input.setValue(s);
					commandSuggestions.updateCommandInfo();
					commandSuggestions.formatChat(s, 0).accept(sink);
					graphics.text(sink.build());
				} else {
					ImGui.text(s);
				}

				ImGui.endTooltip();
			}

			if (ImGui.isItemClicked()) {
				ImGui.setClipboardText(s);
			}
		}

		graphics.popStack();
	}
}
